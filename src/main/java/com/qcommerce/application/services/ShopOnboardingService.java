package com.qcommerce.application.services;

import com.qcommerce.domain.entities.Shop;
import com.qcommerce.infrastructure.persistence.ShopRepository;
import com.qcommerce.interfaces.dto.ShopRegistrationRequest;
import com.qcommerce.interfaces.dto.ShopResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Shop Onboarding Service
 * Handles vendor registration and management
 * 
 * @author Q-Commerce Engineering Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShopOnboardingService {

    private final ShopRepository shopRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Register new shop
     */
    @Transactional
    @CacheEvict(value = {"shop_details", "nearby_products"}, allEntries = true)
    public ShopResponse registerShop(ShopRegistrationRequest request) {
        log.info("Registering new shop - Name: {}, Email: {}", 
            request.getShopName(), request.getEmail());

        // Check if email already exists
        if (shopRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Shop with email already exists");
        }

        // Create Point geometry
        Point location = geometryFactory.createPoint(
            new Coordinate(request.getLongitude(), request.getLatitude())
        );

        // Build shop entity
        Shop shop = Shop.builder()
            .shopName(request.getShopName())
            .ownerName(request.getOwnerName())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .location(location)
            .addressLine1(request.getAddressLine1())
            .addressLine2(request.getAddressLine2())
            .city(request.getCity())
            .state(request.getState())
            .pincode(request.getPincode())
            .gstNumber(request.getGstNumber())
            .fssaiLicense(request.getFssaiLicense())
            .openingTime(request.getOpeningTime())
            .closingTime(request.getClosingTime())
            .deliveryRadiusKm(request.getDeliveryRadiusKm())
            .minOrderAmount(request.getMinOrderAmount())
            .shopStatus(Shop.ShopStatus.PENDING)
            .build();

        Shop saved = shopRepository.save(shop);
        log.info("✅ Shop registered - ID: {}, Status: PENDING", saved.getId());

        return toResponse(saved);
    }

    /**
     * Get shop by ID
     */
    @Cacheable(value = "shop_details", key = "#shopId")
    public ShopResponse getShopById(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found"));
        return toResponse(shop);
    }

    /**
     * Find nearby shops
     */
    public Page<ShopResponse> findNearbyShops(
        Double latitude, 
        Double longitude, 
        Double radiusKm, 
        Pageable pageable
    ) {
        Point userLocation = geometryFactory.createPoint(
            new Coordinate(longitude, latitude)
        );

        List<Shop> shops = shopRepository.findNearbyShops(userLocation, radiusKm);

        List<ShopResponse> responses = shops.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());
        
        List<ShopResponse> pageContent = responses.subList(start, end);
        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    /**
     * Update shop status (approve/reject)
     */
    @Transactional
    @CacheEvict(value = {"shop_details", "nearby_products"}, allEntries = true)
    public ShopResponse updateShopStatus(Long shopId, String status) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        Shop.ShopStatus newStatus = Shop.ShopStatus.valueOf(status.toUpperCase());
        shop.setShopStatus(newStatus);

        Shop updated = shopRepository.save(shop);
        log.info("✅ Shop status updated - ID: {}, Status: {}", shopId, newStatus);

        return toResponse(updated);
    }

    /**
     * Update shop details
     */
    @Transactional
    @CacheEvict(value = {"shop_details", "nearby_products"}, allEntries = true)
    public ShopResponse updateShop(Long shopId, ShopRegistrationRequest request) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        // Update location if changed
        Point location = geometryFactory.createPoint(
            new Coordinate(request.getLongitude(), request.getLatitude())
        );

        shop.setShopName(request.getShopName());
        shop.setOwnerName(request.getOwnerName());
        shop.setPhoneNumber(request.getPhoneNumber());
        shop.setLocation(location);
        shop.setAddressLine1(request.getAddressLine1());
        shop.setAddressLine2(request.getAddressLine2());
        shop.setCity(request.getCity());
        shop.setState(request.getState());
        shop.setPincode(request.getPincode());
        shop.setGstNumber(request.getGstNumber());
        shop.setFssaiLicense(request.getFssaiLicense());
        shop.setOpeningTime(request.getOpeningTime());
        shop.setClosingTime(request.getClosingTime());
        shop.setDeliveryRadiusKm(request.getDeliveryRadiusKm());
        shop.setMinOrderAmount(request.getMinOrderAmount());

        Shop updated = shopRepository.save(shop);
        return toResponse(updated);
    }

    /**
     * Get all shops with filters
     */
    public Page<ShopResponse> getAllShops(String status, String city, Pageable pageable) {
        List<Shop> shops;

        if (status != null && !status.isEmpty()) {
            Shop.ShopStatus shopStatus = Shop.ShopStatus.valueOf(status.toUpperCase());
            shops = shopRepository.findByShopStatus(shopStatus);
        } else if (city != null && !city.isEmpty()) {
            shops = shopRepository.findApprovedShopsByCity(city);
        } else {
            shops = shopRepository.findAll();
        }

        List<ShopResponse> responses = shops.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());
        
        List<ShopResponse> pageContent = responses.subList(start, end);
        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    /**
     * Convert entity to response DTO
     */
    private ShopResponse toResponse(Shop shop) {
        return ShopResponse.builder()
            .id(shop.getId())
            .shopName(shop.getShopName())
            .ownerName(shop.getOwnerName())
            .email(shop.getEmail())
            .phoneNumber(shop.getPhoneNumber())
            .latitude(shop.getLatitude())
            .longitude(shop.getLongitude())
            .addressLine1(shop.getAddressLine1())
            .addressLine2(shop.getAddressLine2())
            .city(shop.getCity())
            .state(shop.getState())
            .pincode(shop.getPincode())
            .gstNumber(shop.getGstNumber())
            .fssaiLicense(shop.getFssaiLicense())
            .shopStatus(shop.getShopStatus().name())
            .openingTime(shop.getOpeningTime())
            .closingTime(shop.getClosingTime())
            .isOpen(shop.getIsOpen())
            .isOperational(shop.isOperational())
            .deliveryRadiusKm(shop.getDeliveryRadiusKm())
            .minOrderAmount(shop.getMinOrderAmount())
            .createdAt(shop.getCreatedAt())
            .updatedAt(shop.getUpdatedAt())
            .build();
    }
}
