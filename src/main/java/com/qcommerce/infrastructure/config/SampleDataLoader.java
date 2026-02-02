package com.qcommerce.infrastructure.config;

import com.qcommerce.domain.entities.*;
import com.qcommerce.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

/**
 * Sample Data Loader (Optional)
 * 
 * Loads sample shops, products, and inventory for testing
 * Only runs in 'dev' profile
 * 
 * To enable: Add --spring.profiles.active=dev to your run command
 */
@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class SampleDataLoader {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Bean
    CommandLineRunner loadSampleData(
        ShopRepository shopRepository,
        ProductRepository productRepository,
        ProductVariantRepository variantRepository,
        InventoryRepository inventoryRepository
    ) {
        return args -> {
            log.info("[v0] Loading sample data for development...");
            
            if (shopRepository.count() > 0) {
                log.info("[v0] Sample data already exists, skipping...");
                return;
            }
            
            // Create sample shop in Mumbai
            Point shopLocation = geometryFactory.createPoint(new Coordinate(72.8777, 19.0760));
            Shop shop = Shop.builder()
                .shopName("QuickMart Mumbai Central")
                .ownerName("Rajesh Kumar")
                .ownerEmail("rajesh@quickmart.com")
                .phoneNumber("+919876543210")
                .address("123 MG Road, Mumbai Central, Mumbai - 400008")
                .location(shopLocation)
                .isActive(true)
                .deliveryRadius(2.0)
                .averageDeliveryTime(15)
                .build();
            shop = shopRepository.save(shop);
            log.info("[v0] Created sample shop: {}", shop.getShopName());
            
            // Create sample product
            Product product = Product.builder()
                .shopId(shop.getId())
                .productName("Fresh Milk")
                .description("Farm fresh full cream milk")
                .categoryId(1L)
                .imageUrl("https://example.com/milk.jpg")
                .isActive(true)
                .build();
            product = productRepository.save(product);
            log.info("[v0] Created sample product: {}", product.getProductName());
            
            // Create product variant
            ProductVariant variant = ProductVariant.builder()
                .productId(product.getId())
                .variantName("1 Liter")
                .size("1L")
                .weight("1000ml")
                .price(new BigDecimal("60.00"))
                .mrp(new BigDecimal("65.00"))
                .sku("MILK-1L-001")
                .build();
            variant = variantRepository.save(variant);
            log.info("[v0] Created sample variant: {}", variant.getVariantName());
            
            // Create inventory
            Inventory inventory = Inventory.builder()
                .shopId(shop.getId())
                .productVariantId(variant.getId())
                .availableQuantity(100)
                .reservedQuantity(0)
                .reorderLevel(20)
                .maxStockLevel(200)
                .build();
            inventoryRepository.save(inventory);
            log.info("[v0] Created sample inventory with 100 units");
            
            log.info("[v0] Sample data loaded successfully!");
        };
    }
}
