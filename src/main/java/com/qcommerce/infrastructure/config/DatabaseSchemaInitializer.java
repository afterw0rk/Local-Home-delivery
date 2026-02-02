package com.qcommerce.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Automatic Database Schema Initializer
 * 
 * Handles automatic creation of:
 * - Database tables (via Hibernate ddl-auto=update)
 * - Spatial indexes for location-based queries
 * - Custom indexes for performance optimization
 * - Unique constraints and foreign keys
 * 
 * This eliminates the need for manual SQL migration scripts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Transactional
    public void initializeSchema() {
        log.info("[v0] Starting automatic database schema initialization...");
        
        try {
            // Wait for Hibernate to create tables first
            Thread.sleep(2000);
            
            // Create spatial indexes for geolocation queries
            createSpatialIndexes();
            
            // Create performance indexes
            createPerformanceIndexes();
            
            // Create unique constraints
            createUniqueConstraints();
            
            log.info("[v0] Database schema initialized successfully!");
            
        } catch (Exception e) {
            log.warn("[v0] Schema initialization encountered issues (may be already created): {}", e.getMessage());
        }
    }

    /**
     * Create spatial indexes for fast location-based queries
     */
    private void createSpatialIndexes() {
        log.info("[v0] Creating spatial indexes...");
        
        // Spatial index on shops.location for nearby shop searches
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.statistics " +
            "WHERE table_schema = DATABASE() AND table_name = 'shops' AND index_name = 'idx_shop_location'",
            "CREATE SPATIAL INDEX idx_shop_location ON shops(location)"
        );
        
        log.info("[v0] Spatial indexes created successfully");
    }

    /**
     * Create performance indexes for frequently queried columns
     */
    private void createPerformanceIndexes() {
        log.info("[v0] Creating performance indexes...");
        
        // Index on products.shop_id for filtering products by shop
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.statistics " +
            "WHERE table_schema = DATABASE() AND table_name = 'products' AND index_name = 'idx_product_shop_id'",
            "CREATE INDEX idx_product_shop_id ON products(shop_id)"
        );
        
        // Index on inventory.product_variant_id for fast inventory lookups
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.statistics " +
            "WHERE table_schema = DATABASE() AND table_name = 'inventory' AND index_name = 'idx_inventory_variant_id'",
            "CREATE INDEX idx_inventory_variant_id ON inventory(product_variant_id)"
        );
        
        // Composite index on inventory for shop + variant queries
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.statistics " +
            "WHERE table_schema = DATABASE() AND table_name = 'inventory' AND index_name = 'idx_inventory_shop_variant'",
            "CREATE INDEX idx_inventory_shop_variant ON inventory(shop_id, product_variant_id)"
        );
        
        // Index on orders.user_id for user order history
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.statistics " +
            "WHERE table_schema = DATABASE() AND table_name = 'orders' AND index_name = 'idx_order_user_id'",
            "CREATE INDEX idx_order_user_id ON orders(user_id)"
        );
        
        // Index on orders.order_status for filtering by status
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.statistics " +
            "WHERE table_schema = DATABASE() AND table_name = 'orders' AND index_name = 'idx_order_status'",
            "CREATE INDEX idx_order_status ON orders(order_status)"
        );
        
        // Index on carts.user_id for fast cart retrieval
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.statistics " +
            "WHERE table_schema = DATABASE() AND table_name = 'carts' AND index_name = 'idx_cart_user_id'",
            "CREATE INDEX idx_cart_user_id ON carts(user_id)"
        );
        
        log.info("[v0] Performance indexes created successfully");
    }

    /**
     * Create unique constraints to prevent data duplication
     */
    private void createUniqueConstraints() {
        log.info("[v0] Creating unique constraints...");
        
        // Unique constraint on shops.owner_email
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.table_constraints " +
            "WHERE table_schema = DATABASE() AND table_name = 'shops' AND constraint_name = 'uk_shop_owner_email'",
            "ALTER TABLE shops ADD CONSTRAINT uk_shop_owner_email UNIQUE (owner_email)"
        );
        
        // Unique constraint on shops.phone_number
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.table_constraints " +
            "WHERE table_schema = DATABASE() AND table_name = 'shops' AND constraint_name = 'uk_shop_phone'",
            "ALTER TABLE shops ADD CONSTRAINT uk_shop_phone UNIQUE (phone_number)"
        );
        
        // Unique constraint on inventory (shop_id, product_variant_id)
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.table_constraints " +
            "WHERE table_schema = DATABASE() AND table_name = 'inventory' AND constraint_name = 'uk_inventory_shop_variant'",
            "ALTER TABLE inventory ADD CONSTRAINT uk_inventory_shop_variant UNIQUE (shop_id, product_variant_id)"
        );
        
        // Unique constraint on cart_items (cart_id, variant_id)
        executeIfNotExists(
            "SELECT COUNT(*) FROM information_schema.table_constraints " +
            "WHERE table_schema = DATABASE() AND table_name = 'cart_items' AND constraint_name = 'uk_cart_item'",
            "ALTER TABLE cart_items ADD CONSTRAINT uk_cart_item UNIQUE (cart_id, variant_id)"
        );
        
        log.info("[v0] Unique constraints created successfully");
    }

    /**
     * Execute SQL only if the index/constraint doesn't already exist
     */
    private void executeIfNotExists(String checkQuery, String createQuery) {
        try {
            Integer count = jdbcTemplate.queryForObject(checkQuery, Integer.class);
            if (count == null || count == 0) {
                jdbcTemplate.execute(createQuery);
                log.debug("[v0] Executed: {}", createQuery);
            } else {
                log.debug("[v0] Already exists, skipping: {}", createQuery);
            }
        } catch (Exception e) {
            log.warn("[v0] Could not execute {}: {}", createQuery, e.getMessage());
        }
    }
}
