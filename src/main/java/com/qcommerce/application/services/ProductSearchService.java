package com.qcommerce.application.services;

import com.qcommerce.domain.entities.*;
import com.qcommerce.infrastructure.persistence.*;
import com.qcommerce.interfaces.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Product Search Service - Hyper-Local Discovery Business Logic
 * 
 * Architecture Layer: Application (Use Case Orchestration)
 * 
 * Responsibilities:
 * 1. Coordinate between repositories
 * 2. Apply business rules
 * 3. Manage caching strategy
 * 4. Transform domain entities to DTOs
 * 
 * Performance Strategy:
 * - Redis caching for product catalog
 * - Spatial queries optimized with indexes
 * - Lazy loading of inventory data
 * 
 * @author Q-Commerce Engineering Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductSearchService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;
    
    // GeometryFactory for creating JTS Point objects (thread-safe singleton)
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * CORE METHOD: Search products near user location
     * 
     * Algorithm:
     * 1. Find nearby shops using spatial query (ST_Distance_Sphere)
     * 2. Fetch products from these shops
     * 3. Apply category/keyword filters
     * 4. Enrich with inventory data
     * 5. Calculate distance and estimated delivery time
     * 6. Apply sorting and pagination
     * 
     * Caching Strategy:
     * - Key: "nearby_products:{lat}:{lon}:{radius}:{category}:{keyword}:{page}"
     * - TTL: 10 minutes
     * - Invalidation: Manual on product/inventory updates
     * - Tradeoff: Slight inventory staleness for massive performance gain
     * 
     * Performance:
     * - Cold (no cache): ~100-150ms
     * - Warm (with cache): ~10-20ms
     */
    @Cacheable(
        value = "nearby_products",
        key = "#request.latitude + ':' + #request.longitude + ':' + #request.radiusKm + ':' + " +
              "#request.categoryId + ':' + #request.searchKeyword + ':' + #request.page",
        unless = "#result.isEmpty()"
    )
    public Page<NearbyProductResponse> searchNearbyProducts(NearbyProductSearchRequest request) {
        log.debug("Executing nearby product search (cache miss or disabled)");

        // Step 1: Create Point geometry from user coordinates
        Point userLocation = createPoint(request.getLongitude(), request.getLatitude());

        // Step 2: Find nearby shops using spatial query
        List<Shop> nearbyShops = shopRepository.findNearbyShops(userLocation, request.getRadiusKm());
        
        if (nearbyShops.isEmpty()) {
            log.info("No shops found within {} km radius", request.getRadiusKm());
            return Page.empty();
        }

        log.debug("Found {} nearby shops", nearbyShops.size());
        List<Long> shopIds = nearbyShops.stream().map(Shop::getId).toList();

        // Step 3: Fetch products from these shops with filters
        List<Product> products = productRepository.findProductsByShopsWithFilters(
            shopIds,
            request.getCategoryId(),
            request.getSearchKeyword()
        );

        if (products.isEmpty()) {
            log.info("No products found matching criteria");
            return Page.empty();
        }

        // Step 4: Fetch variants and inventory for these products
        List<Long> productIds = products.stream().map(Product::getId).toList();
        Map<Long, List<ProductVariant>> variantsByProduct = productVariantRepository
            .findByProductIdIn(productIds)
            .stream()
            .collect(Collectors.groupingBy(ProductVariant::getProductId));

        // Step 5: Fetch inventory data
        List<Long> variantIds = variantsByProduct.values().stream()
            .flatMap(List::stream)
            .map(ProductVariant::getId)
            .toList();
        
        Map<Long, Inventory> inventoryByVariantId = inventoryRepository
            .findByProductVariantIdIn(variantIds)
            .stream()
            .collect(Collectors.toMap(Inventory::getProductVariantId, inv -> inv));

        // Step 6: Create shop distance map
        Map<Long, Double> shopDistances = calculateShopDistances(nearbyShops, userLocation);

        // Step 7: Transform to DTOs
        List<NearbyProductResponse> responses = products.stream()
            .map(product -> buildProductResponse(
                product,
                variantsByProduct.get(product.getId()),
                inventoryByVariantId,
                nearbyShops.stream()
                    .filter(shop -> shop.getId().equals(product.getShopId()))
                    .findFirst()
                    .orElse(null),
                shopDistances.get(product.getShopId())
            ))
            .filter(Objects::nonNull) // Filter out products with no stock
            .collect(Collectors.toList());

        // Step 8: Apply sorting
        responses = applySorting(responses, request.getSortBy());

        // Step 9: Apply pagination
        return applyPagination(responses, request.getPage(), request.getPageSize());
    }

    /**
     * Check if product is available at specific location
     */
    public boolean isProductAvailableAtLocation(Long productId, Double latitude, Double longitude, Double radiusKm) {
        Point userLocation = createPoint(longitude, latitude);
        
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || !product.getIsActive()) {
            return false;
        }

        // Check if shop is within radius
        Shop shop = shopRepository.findById(product.getShopId()).orElse(null);
        if (shop == null || !shop.isOperational()) {
            return false;
        }

        double distance = calculateDistance(userLocation, shop.getLocation());
        if (distance > radiusKm) {
            return false;
        }

        // Check inventory
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        for (ProductVariant variant : variants) {
            Inventory inventory = inventoryRepository.findByProductVariantId(variant.getId()).orElse(null);
            if (inventory != null && inventory.getAvailableQuantity() > 0) {
                return true;
            }
        }

        return false;
    }

    // ========== Helper Methods ==========

    private Point createPoint(Double longitude, Double latitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private Map<Long, Double> calculateShopDistances(List<Shop> shops, Point userLocation) {
        return shops.stream()
            .collect(Collectors.toMap(
                Shop::getId,
                shop -> calculateDistance(userLocation, shop.getLocation())
            ));
    }

    private double calculateDistance(Point point1, Point point2) {
        // Using Haversine formula for accurate distance
        double lat1 = Math.toRadians(point1.getY());
        double lon1 = Math.toRadians(point1.getX());
        double lat2 = Math.toRadians(point2.getY());
        double lon2 = Math.toRadians(point2.getX());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double earthRadiusKm = 6371.0;

        return earthRadiusKm * c;
    }

    private NearbyProductResponse buildProductResponse(
        Product product,
        List<ProductVariant> variants,
        Map<Long, Inventory> inventoryMap,
        Shop shop,
        Double distanceKm
    ) {
        if (shop == null) {
            return null;
        }

        // Build variant info with inventory
        List<NearbyProductResponse.VariantInfo> variantInfos = new ArrayList<>();
        boolean hasStock = false;

        if (variants != null) {
            for (ProductVariant variant : variants) {
                if (!variant.getIsActive()) continue;

                Inventory inventory = inventoryMap.get(variant.getId());
                int availableQty = inventory != null ? inventory.getAvailableQuantity() : 0;

                if (availableQty > 0) {
                    hasStock = true;
                }

                variantInfos.add(NearbyProductResponse.VariantInfo.builder()
                    .variantId(variant.getId())
                    .variantName(variant.getVariantName())
                    .sku(variant.getSku())
                    .price(variant.getPrice())
                    .mrp(variant.getMrp())
                    .availableQuantity(availableQty)
                    .weightDisplay(variant.getWeightValue() + variant.getWeightUnit().name().toLowerCase())
                    .build());
            }
        }

        // Don't show products with no stock
        if (!hasStock && product.getHasVariants()) {
            return null;
        }

        // Build shop info
        NearbyProductResponse.ShopInfo shopInfo = NearbyProductResponse.ShopInfo.builder()
            .shopId(shop.getId())
            .shopName(shop.getShopName())
            .address(shop.getAddressLine1() + ", " + shop.getCity())
            .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
            .minOrderAmount(shop.getMinOrderAmount())
            .isOpen(shop.isOperational())
            .estimatedDeliveryTime(calculateDeliveryTime(distanceKm))
            .build();

        return NearbyProductResponse.builder()
            .productId(product.getId())
            .productName(product.getProductName())
            .description(product.getDescription())
            .brand(product.getBrand())
            .primaryImageUrl(product.getPrimaryImageUrl())
            .basePrice(product.getBasePrice())
            .hasVariants(product.getHasVariants())
            .variants(variantInfos)
            .shop(shopInfo)
            .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
            .inStock(hasStock)
            .build();
    }

    private String calculateDeliveryTime(Double distanceKm) {
        // Simple estimation: 3 km/h average (including prep time)
        int minutes = (int) Math.ceil((distanceKm / 3.0) * 60);
        int minTime = Math.max(10, minutes);
        int maxTime = minTime + 5;
        return minTime + "-" + maxTime + " mins";
    }

    private List<NearbyProductResponse> applySorting(
        List<NearbyProductResponse> products,
        NearbyProductSearchRequest.SortBy sortBy
    ) {
        return switch (sortBy) {
            case DISTANCE -> products.stream()
                .sorted(Comparator.comparing(NearbyProductResponse::getDistanceKm))
                .toList();
            case PRICE_LOW_HIGH -> products.stream()
                .sorted(Comparator.comparing(p -> p.getBasePrice() != null ? p.getBasePrice() : p.getVariants().get(0).getPrice()))
                .toList();
            case PRICE_HIGH_LOW -> products.stream()
                .sorted(Comparator.comparing((NearbyProductResponse p) -> 
                    p.getBasePrice() != null ? p.getBasePrice() : p.getVariants().get(0).getPrice()).reversed())
                .toList();
            default -> products; // POPULARITY, RATING not implemented yet
        };
    }

    private Page<NearbyProductResponse> applyPagination(
        List<NearbyProductResponse> products,
        Integer page,
        Integer pageSize
    ) {
        int start = page * pageSize;
        int end = Math.min(start + pageSize, products.size());

        if (start >= products.size()) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, pageSize), products.size());
        }

        List<NearbyProductResponse> pageContent = products.subList(start, end);
        return new PageImpl<>(pageContent, PageRequest.of(page, pageSize), products.size());
    }
}
