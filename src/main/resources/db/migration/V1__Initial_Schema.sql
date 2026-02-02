-- =====================================================
-- Q-Commerce Platform Database Schema
-- MySQL 8.0+ with Spatial Support
-- =====================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS qcommerce_db;
USE qcommerce_db;

-- =====================================================
-- SHOPS TABLE (Vendor/Shopkeeper Management)
-- =====================================================
CREATE TABLE shops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_name VARCHAR(255) NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    
    -- Spatial Data: Location stored as POINT (Longitude, Latitude)
    location POINT NOT NULL SRID 4326,
    
    address_line1 VARCHAR(500) NOT NULL,
    address_line2 VARCHAR(500),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    
    -- Business Details
    gst_number VARCHAR(50),
    fssai_license VARCHAR(50),
    shop_status ENUM('PENDING', 'APPROVED', 'SUSPENDED', 'REJECTED') DEFAULT 'PENDING',
    
    -- Operational Hours (JSON or separate table in production)
    opening_time TIME,
    closing_time TIME,
    is_open BOOLEAN DEFAULT TRUE,
    
    -- Delivery Configuration
    delivery_radius_km DECIMAL(5,2) DEFAULT 2.00,
    min_order_amount DECIMAL(10,2) DEFAULT 0.00,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version INT DEFAULT 0,
    
    -- Spatial Index for Geo-Queries (CRITICAL for performance)
    SPATIAL INDEX idx_shop_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- CATEGORIES TABLE (Product Classification)
-- =====================================================
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL,
    parent_category_id BIGINT,
    category_image_url VARCHAR(500),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_category_parent (parent_category_id),
    INDEX idx_category_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PRODUCTS TABLE (Product Master)
-- =====================================================
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    
    product_name VARCHAR(500) NOT NULL,
    description TEXT,
    brand VARCHAR(255),
    
    -- Product Images
    primary_image_url VARCHAR(500),
    additional_images JSON,
    
    -- Variants Support (e.g., 500g, 1kg, Small, Medium)
    has_variants BOOLEAN DEFAULT FALSE,
    
    -- Base Price (if no variants)
    base_price DECIMAL(10,2),
    
    -- Search & Discovery
    tags JSON,
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    
    -- SEO
    slug VARCHAR(500) UNIQUE,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    
    INDEX idx_product_shop (shop_id),
    INDEX idx_product_category (category_id),
    INDEX idx_product_active (is_active),
    INDEX idx_product_slug (slug),
    FULLTEXT INDEX idx_product_search (product_name, description, brand)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PRODUCT_VARIANTS TABLE (SKU-level Management)
-- =====================================================
CREATE TABLE product_variants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    
    -- Variant Attributes
    variant_name VARCHAR(255) NOT NULL, -- e.g., "500g", "1L", "Red-XL"
    sku VARCHAR(100) NOT NULL UNIQUE,
    
    -- Pricing
    price DECIMAL(10,2) NOT NULL,
    mrp DECIMAL(10,2) NOT NULL,
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    
    -- Weight/Size
    weight_value DECIMAL(10,2),
    weight_unit ENUM('g', 'kg', 'ml', 'l', 'pcs') DEFAULT 'pcs',
    
    -- Variant Image
    variant_image_url VARCHAR(500),
    
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_variant_product (product_id),
    INDEX idx_variant_sku (sku)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INVENTORY TABLE (Stock Management with Optimistic Locking)
-- =====================================================
CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_variant_id BIGINT NOT NULL UNIQUE,
    shop_id BIGINT NOT NULL,
    
    -- Stock Levels
    available_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0, -- Items in cart but not yet ordered
    
    -- Thresholds
    reorder_level INT DEFAULT 10,
    max_stock_level INT DEFAULT 1000,
    
    -- Flash Sale Protection: Optimistic Locking
    version INT NOT NULL DEFAULT 0,
    
    -- Last Stock Update
    last_restocked_at TIMESTAMP,
    last_updated_by VARCHAR(255),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE,
    
    INDEX idx_inventory_variant (product_variant_id),
    INDEX idx_inventory_shop (shop_id),
    INDEX idx_inventory_available (available_quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- USERS TABLE (Customer Management)
-- =====================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    
    -- Default Delivery Location
    default_location POINT SRID 4326,
    
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    
    SPATIAL INDEX idx_user_location (default_location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- USER_ADDRESSES TABLE (Saved Delivery Addresses)
-- =====================================================
CREATE TABLE user_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    
    address_label VARCHAR(50), -- 'Home', 'Office', 'Other'
    location POINT NOT NULL SRID 4326,
    
    address_line1 VARCHAR(500) NOT NULL,
    address_line2 VARCHAR(500),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    
    landmark VARCHAR(255),
    contact_phone VARCHAR(20),
    
    is_default BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_address_user (user_id),
    SPATIAL INDEX idx_address_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ORDERS TABLE (Order Management)
-- =====================================================
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    
    -- Delivery Address
    delivery_address_id BIGINT NOT NULL,
    
    -- Order Status Flow
    order_status ENUM(
        'CREATED',
        'PAYMENT_PENDING',
        'PAYMENT_CONFIRMED',
        'PREPARING',
        'OUT_FOR_DELIVERY',
        'DELIVERED',
        'CANCELLED',
        'REFUNDED'
    ) DEFAULT 'CREATED',
    
    -- Financial Details
    subtotal DECIMAL(10,2) NOT NULL,
    delivery_fee DECIMAL(10,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    
    -- Payment
    payment_method ENUM('COD', 'UPI', 'CARD', 'WALLET') DEFAULT 'COD',
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    payment_id VARCHAR(255),
    
    -- Delivery Tracking
    estimated_delivery_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    delivery_person_id BIGINT,
    
    -- Customer Notes
    special_instructions TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(500),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE RESTRICT,
    FOREIGN KEY (delivery_address_id) REFERENCES user_addresses(id) ON DELETE RESTRICT,
    
    INDEX idx_order_user (user_id),
    INDEX idx_order_shop (shop_id),
    INDEX idx_order_status (order_status),
    INDEX idx_order_created (created_at),
    INDEX idx_order_number (order_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ORDER_ITEMS TABLE (Order Line Items)
-- =====================================================
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    
    product_name VARCHAR(500) NOT NULL,
    variant_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id) ON DELETE RESTRICT,
    
    INDEX idx_order_item_order (order_id),
    INDEX idx_order_item_variant (product_variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ORDER_STATUS_HISTORY TABLE (Audit Trail for Real-time Tracking)
-- =====================================================
CREATE TABLE order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    
    changed_by VARCHAR(255),
    change_reason VARCHAR(500),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_status_history_order (order_id),
    INDEX idx_status_history_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Sample Data Insertion
-- =====================================================

-- Insert Sample Categories
INSERT INTO categories (category_name, parent_category_id) VALUES
('Groceries', NULL),
('Fruits & Vegetables', 1),
('Dairy & Bakery', 1),
('Personal Care', NULL),
('Home & Kitchen', NULL);

-- Insert Sample Shop with Spatial Data
-- Location: Bangalore (12.9716° N, 77.5946° E)
INSERT INTO shops (shop_name, owner_name, email, phone_number, location, address_line1, city, state, pincode, shop_status, delivery_radius_km)
VALUES (
    'Quick Mart Express',
    'Rajesh Kumar',
    'rajesh@quickmart.com',
    '+919876543210',
    ST_GeomFromText('POINT(77.5946 12.9716)', 4326),
    '123, MG Road',
    'Bangalore',
    'Karnataka',
    '560001',
    'APPROVED',
    3.00
);

-- Insert Sample User
INSERT INTO users (full_name, email, phone_number, password_hash, default_location, phone_verified)
VALUES (
    'John Doe',
    'john.doe@example.com',
    '+919123456789',
    '$2a$10$dummyhashforpassword',
    ST_GeomFromText('POINT(77.5950 12.9720)', 4326),
    TRUE
);
