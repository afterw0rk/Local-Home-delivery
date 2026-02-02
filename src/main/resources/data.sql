-- ========================================
-- Q-Commerce Platform - Sample Data (Schema-Aligned with Timestamps)
-- ========================================
-- This file is automatically executed by Spring Boot after Hibernate creates tables
-- All column names match JPA entity definitions including created_at/updated_at

-- ========================================
-- SHOPS (with JTS Point Spatial Data + Timestamps)
-- ========================================

-- Shop 1: Fresh Mart (Downtown Bangalore)
INSERT INTO shops (shop_name, owner_name, email, phone_number, location, address_line1, address_line2, city, state, pincode, gst_number, fssai_license, shop_status, opening_time, closing_time, is_open, delivery_radius_km, min_order_amount, created_at, updated_at, created_by, updated_by, version)
VALUES (
    'Fresh Mart Downtown',
    'Rajesh Kumar',
    'rajesh@freshmart.com',
    '+91-9876543210',
    ST_GeomFromText('POINT(77.5946 12.9716)', 4326),
    '123 MG Road',
    'Near Trinity Metro Station',
    'Bangalore',
    'Karnataka',
    '560001',
    '29ABCDE1234F1Z5',
    '12345678901234',
    'APPROVED',
    '08:00:00',
    '22:00:00',
    true,
    5.0,
    0.00,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM',
    0
);

-- Shop 2: Quick Grocery (Indiranagar)
INSERT INTO shops (shop_name, owner_name, email, phone_number, location, address_line1, address_line2, city, state, pincode, gst_number, fssai_license, shop_status, opening_time, closing_time, is_open, delivery_radius_km, min_order_amount, created_at, updated_at, created_by, updated_by, version)
VALUES (
    'Quick Grocery Indiranagar',
    'Priya Sharma',
    'priya@quickgrocery.com',
    '+91-9876543211',
    ST_GeomFromText('POINT(77.6412 12.9784)', 4326),
    '456 CMH Road',
    'Opposite 100 Feet Road',
    'Bangalore',
    'Karnataka',
    '560038',
    '29BCDEF2345G1Z6',
    '23456789012345',
    'APPROVED',
    '07:00:00',
    '23:00:00',
    true,
    3.0,
    0.00,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM',
    0
);

-- Shop 3: Super Store (Koramangala)
INSERT INTO shops (shop_name, owner_name, email, phone_number, location, address_line1, address_line2, city, state, pincode, gst_number, fssai_license, shop_status, opening_time, closing_time, is_open, delivery_radius_km, min_order_amount, created_at, updated_at, created_by, updated_by, version)
VALUES (
    'Super Store Koramangala',
    'Amit Patel',
    'amit@superstore.com',
    '+91-9876543212',
    ST_GeomFromText('POINT(77.6175 12.9352)', 4326),
    '789 80 Feet Road',
    '5th Block Koramangala',
    'Bangalore',
    'Karnataka',
    '560034',
    '29CDEFG3456H1Z7',
    '34567890123456',
    'APPROVED',
    '06:00:00',
    '23:30:00',
    true,
    4.0,
    0.00,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM',
    0
);

-- Shop 4: Daily Needs (Whitefield)
INSERT INTO shops (shop_name, owner_name, email, phone_number, location, address_line1, address_line2, city, state, pincode, gst_number, fssai_license, shop_status, opening_time, closing_time, is_open, delivery_radius_km, min_order_amount, created_at, updated_at, created_by, updated_by, version)
VALUES (
    'Daily Needs Whitefield',
    'Sneha Reddy',
    'sneha@dailyneeds.com',
    '+91-9876543213',
    ST_GeomFromText('POINT(77.7500 12.9698)', 4326),
    '321 ITPL Main Road',
    'Near Forum Mall',
    'Bangalore',
    'Karnataka',
    '560066',
    '29DEFGH4567I1Z8',
    '45678901234567',
    'APPROVED',
    '08:00:00',
    '22:00:00',
    true,
    6.0,
    0.00,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM',
    0
);

-- ========================================
-- PRODUCTS (with timestamps)
-- ========================================

-- Dairy Products (Shop 1)
INSERT INTO products (product_name, description, brand, shop_id, category_id, primary_image_url, has_variants, is_active, is_featured, created_at, updated_at, version)
VALUES 
('Amul Fresh Milk', 'Fresh full cream milk from local dairy farms', 'Amul', 1, 1, 'https://images.example.com/milk.jpg', true, true, true, NOW(), NOW(), 0),
('Amul Butter', 'Utterly butterly delicious made from pure milk', 'Amul', 1, 1, 'https://images.example.com/butter.jpg', true, true, false, NOW(), NOW(), 0),
('Nestle Yogurt', 'Creamy smooth yogurt with live cultures', 'Nestle', 1, 1, 'https://images.example.com/yogurt.jpg', true, true, false, NOW(), NOW(), 0);

-- Fruits & Vegetables (Shop 1)
INSERT INTO products (product_name, description, brand, shop_id, category_id, primary_image_url, has_variants, is_active, is_featured, created_at, updated_at, version)
VALUES 
('Fresh Banana', 'Organic bananas from local farms in Karnataka', 'Local', 1, 2, 'https://images.example.com/banana.jpg', true, true, true, NOW(), NOW(), 0),
('Red Apple', 'Crispy Shimla apples fresh from Himachal', 'Local', 1, 2, 'https://images.example.com/apple.jpg', true, true, false, NOW(), NOW(), 0),
('Fresh Tomato', 'Farm fresh tomatoes handpicked daily', 'Local', 1, 2, 'https://images.example.com/tomato.jpg', true, true, false, NOW(), NOW(), 0);

-- Snacks (Shop 2)
INSERT INTO products (product_name, description, brand, shop_id, category_id, primary_image_url, has_variants, is_active, is_featured, created_at, updated_at, version)
VALUES 
('Lays Chips', 'Crispy potato chips in classic salted flavor', 'Lays', 2, 3, 'https://images.example.com/lays.jpg', true, true, true, NOW(), NOW(), 0),
('Parle-G Biscuits', 'Classic glucose biscuits perfect with tea', 'Parle', 2, 3, 'https://images.example.com/parleg.jpg', true, true, false, NOW(), NOW(), 0),
('Maggi Noodles', '2-minute instant masala noodles', 'Maggi', 2, 3, 'https://images.example.com/maggi.jpg', true, true, true, NOW(), NOW(), 0);

-- Beverages (Shop 2)
INSERT INTO products (product_name, description, brand, shop_id, category_id, primary_image_url, has_variants, is_active, is_featured, created_at, updated_at, version)
VALUES 
('Coca Cola', 'Classic cola drink for refreshment', 'Coca-Cola', 2, 4, 'https://images.example.com/coke.jpg', true, true, true, NOW(), NOW(), 0),
('Pepsi', 'Refreshing cola with a bold taste', 'Pepsi', 2, 4, 'https://images.example.com/pepsi.jpg', true, true, false, NOW(), NOW(), 0),
('Sprite', 'Crisp lemon-lime flavored drink', 'Sprite', 2, 4, 'https://images.example.com/sprite.jpg', true, true, false, NOW(), NOW(), 0);

-- Staples (Shop 3)
INSERT INTO products (product_name, description, brand, shop_id, category_id, primary_image_url, has_variants, is_active, is_featured, created_at, updated_at, version)
VALUES 
('India Gate Basmati Rice', 'Premium aged basmati rice', 'India Gate', 3, 5, 'https://images.example.com/rice.jpg', true, true, true, NOW(), NOW(), 0),
('Tata Salt', 'Iodized vacuum evaporated table salt', 'Tata', 3, 5, 'https://images.example.com/salt.jpg', true, true, false, NOW(), NOW(), 0),
('Fortune Sunflower Oil', 'Refined sunflower cooking oil', 'Fortune', 3, 5, 'https://images.example.com/oil.jpg', true, true, false, NOW(), NOW(), 0);

-- Personal Care (Shop 4)
INSERT INTO products (product_name, description, brand, shop_id, category_id, primary_image_url, has_variants, is_active, is_featured, created_at, updated_at, version)
VALUES 
('Dove Soap', 'Moisturizing beauty soap with cream', 'Dove', 4, 6, 'https://images.example.com/dove.jpg', true, true, false, NOW(), NOW(), 0),
('Colgate Toothpaste', 'Total advanced health toothpaste', 'Colgate', 4, 6, 'https://images.example.com/colgate.jpg', true, true, true, NOW(), NOW(), 0),
('Pantene Shampoo', 'Pro-V hair fall control shampoo', 'Pantene', 4, 6, 'https://images.example.com/pantene.jpg', true, true, false, NOW(), NOW(), 0);

-- ========================================
-- PRODUCT VARIANTS (with timestamps)
-- ========================================

-- Milk Variants (Product 1)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(1, '500ml Pouch', '500ml', NULL, 'MILK-500ML', 25.00, 25.00, 0.00, 500, 'ML', true, NOW(), NOW()),
(1, '1 Liter Pouch', '1L', NULL, 'MILK-1L', 48.00, 50.00, 4.00, 1, 'L', true, NOW(), NOW());

-- Butter Variants (Product 2)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(2, '100g Pack', NULL, '100g', 'BUTTER-100G', 52.00, 55.00, 5.45, 100, 'G', true, NOW(), NOW()),
(2, '500g Pack', NULL, '500g', 'BUTTER-500G', 250.00, 270.00, 7.41, 500, 'G', true, NOW(), NOW());

-- Yogurt Variants (Product 3)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(3, '200g Cup', NULL, '200g', 'YOGURT-200G', 30.00, 30.00, 0.00, 200, 'G', true, NOW(), NOW()),
(3, '400g Cup', NULL, '400g', 'YOGURT-400G', 55.00, 60.00, 8.33, 400, 'G', true, NOW(), NOW());

-- Banana Variants (Product 4)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(4, '1 Dozen (12 pcs)', NULL, '1.5kg', 'BANANA-12PC', 60.00, 60.00, 0.00, 12, 'PCS', true, NOW(), NOW()),
(4, '6 Pieces', NULL, '750g', 'BANANA-6PC', 35.00, 35.00, 0.00, 6, 'PCS', true, NOW(), NOW());

-- Apple Variants (Product 5)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(5, '1kg Pack', NULL, '1kg', 'APPLE-1KG', 180.00, 200.00, 10.00, 1, 'KG', true, NOW(), NOW()),
(5, '500g Pack', NULL, '500g', 'APPLE-500G', 95.00, 100.00, 5.00, 500, 'G', true, NOW(), NOW());

-- Tomato Variants (Product 6)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(6, '1kg Pack', NULL, '1kg', 'TOMATO-1KG', 40.00, 40.00, 0.00, 1, 'KG', true, NOW(), NOW()),
(6, '500g Pack', NULL, '500g', 'TOMATO-500G', 22.00, 22.00, 0.00, 500, 'G', true, NOW(), NOW());

-- Lays Chips Variants (Product 7)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(7, 'Small Pack', NULL, '25g', 'LAYS-25G', 10.00, 10.00, 0.00, 25, 'G', true, NOW(), NOW()),
(7, 'Medium Pack', NULL, '52g', 'LAYS-52G', 20.00, 20.00, 0.00, 52, 'G', true, NOW(), NOW()),
(7, 'Large Pack', NULL, '90g', 'LAYS-90G', 35.00, 40.00, 12.50, 90, 'G', true, NOW(), NOW());

-- Parle-G Variants (Product 8)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(8, 'Small Pack', NULL, '56g', 'PARLEG-56G', 5.00, 5.00, 0.00, 56, 'G', true, NOW(), NOW()),
(8, 'Family Pack', NULL, '376g', 'PARLEG-376G', 30.00, 35.00, 14.29, 376, 'G', true, NOW(), NOW());

-- Maggi Variants (Product 9)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(9, 'Single Pack', NULL, '70g', 'MAGGI-70G', 14.00, 14.00, 0.00, 70, 'G', true, NOW(), NOW()),
(9, '4-Pack', NULL, '280g', 'MAGGI-280G', 52.00, 56.00, 7.14, 280, 'G', true, NOW(), NOW());

-- Coca Cola Variants (Product 10)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(10, '250ml Bottle', '250ml', NULL, 'COKE-250ML', 20.00, 20.00, 0.00, 250, 'ML', true, NOW(), NOW()),
(10, '750ml Bottle', '750ml', NULL, 'COKE-750ML', 40.00, 40.00, 0.00, 750, 'ML', true, NOW(), NOW()),
(10, '2L Bottle', '2L', NULL, 'COKE-2L', 90.00, 95.00, 5.26, 2, 'L', true, NOW(), NOW());

-- Pepsi Variants (Product 11)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(11, '250ml Bottle', '250ml', NULL, 'PEPSI-250ML', 20.00, 20.00, 0.00, 250, 'ML', true, NOW(), NOW()),
(11, '750ml Bottle', '750ml', NULL, 'PEPSI-750ML', 40.00, 40.00, 0.00, 750, 'ML', true, NOW(), NOW());

-- Sprite Variants (Product 12)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(12, '250ml Bottle', '250ml', NULL, 'SPRITE-250ML', 20.00, 20.00, 0.00, 250, 'ML', true, NOW(), NOW()),
(12, '750ml Bottle', '750ml', NULL, 'SPRITE-750ML', 40.00, 40.00, 0.00, 750, 'ML', true, NOW(), NOW());

-- Basmati Rice Variants (Product 13)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(13, '1kg Pack', NULL, '1kg', 'RICE-1KG', 120.00, 130.00, 7.69, 1, 'KG', true, NOW(), NOW()),
(13, '5kg Bag', NULL, '5kg', 'RICE-5KG', 550.00, 600.00, 8.33, 5, 'KG', true, NOW(), NOW());

-- Salt Variants (Product 14)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(14, '1kg Pack', NULL, '1kg', 'SALT-1KG', 20.00, 20.00, 0.00, 1, 'KG', true, NOW(), NOW());

-- Oil Variants (Product 15)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(15, '1 Liter Bottle', '1L', NULL, 'OIL-1L', 150.00, 160.00, 6.25, 1, 'L', true, NOW(), NOW()),
(15, '5 Liter Can', '5L', NULL, 'OIL-5L', 700.00, 750.00, 6.67, 5, 'L', true, NOW(), NOW());

-- Dove Soap Variants (Product 16)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(16, 'Single Bar', NULL, '100g', 'DOVE-100G', 45.00, 50.00, 10.00, 100, 'G', true, NOW(), NOW()),
(16, '3-Pack', NULL, '300g', 'DOVE-300G', 120.00, 135.00, 11.11, 300, 'G', true, NOW(), NOW());

-- Colgate Variants (Product 17)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(17, '100g Tube', NULL, '100g', 'COLGATE-100G', 85.00, 90.00, 5.56, 100, 'G', true, NOW(), NOW()),
(17, '200g Tube', NULL, '200g', 'COLGATE-200G', 155.00, 165.00, 6.06, 200, 'G', true, NOW(), NOW());

-- Pantene Variants (Product 18)
INSERT INTO product_variants (product_id, variant_name, size, weight, sku, price, mrp, discount_percentage, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(18, '180ml Bottle', '180ml', NULL, 'PANTENE-180ML', 175.00, 190.00, 7.89, 180, 'ML', true, NOW(), NOW()),
(18, '340ml Bottle', '340ml', NULL, 'PANTENE-340ML', 320.00, 350.00, 8.57, 340, 'ML', true, NOW(), NOW());

-- ========================================
-- INVENTORY (with Optimistic Locking + Timestamps)
-- ========================================

-- Fresh Mart Inventory (Shop 1) - 12 variants
INSERT INTO inventory (product_variant_id, shop_id, available_quantity, reserved_quantity, reorder_level, max_stock_level, created_at, updated_at, version)
VALUES 
(1, 1, 150, 0, 20, 500, NOW(), NOW(), 0),
(2, 1, 80, 0, 15, 300, NOW(), NOW(), 0),
(3, 1, 100, 0, 10, 400, NOW(), NOW(), 0),
(4, 1, 45, 0, 5, 200, NOW(), NOW(), 0),
(5, 1, 120, 0, 15, 500, NOW(), NOW(), 0),
(6, 1, 60, 0, 10, 300, NOW(), NOW(), 0),
(7, 1, 200, 0, 30, 800, NOW(), NOW(), 0),
(8, 1, 180, 0, 25, 600, NOW(), NOW(), 0),
(9, 1, 90, 0, 15, 400, NOW(), NOW(), 0),
(10, 1, 140, 0, 20, 500, NOW(), NOW(), 0),
(11, 1, 250, 0, 40, 1000, NOW(), NOW(), 0),
(12, 1, 200, 0, 30, 800, NOW(), NOW(), 0);

-- Quick Grocery Inventory (Shop 2) - 14 variants
INSERT INTO inventory (product_variant_id, shop_id, available_quantity, reserved_quantity, reorder_level, max_stock_level, created_at, updated_at, version)
VALUES 
(13, 2, 300, 0, 50, 1000, NOW(), NOW(), 0),
(14, 2, 200, 0, 30, 800, NOW(), NOW(), 0),
(15, 2, 120, 0, 20, 500, NOW(), NOW(), 0),
(16, 2, 500, 0, 80, 2000, NOW(), NOW(), 0),
(17, 2, 150, 0, 25, 600, NOW(), NOW(), 0),
(18, 2, 250, 0, 40, 1000, NOW(), NOW(), 0),
(19, 2, 100, 0, 15, 500, NOW(), NOW(), 0),
(20, 2, 180, 0, 30, 600, NOW(), NOW(), 0),
(21, 2, 90, 0, 15, 400, NOW(), NOW(), 0),
(22, 2, 50, 0, 10, 200, NOW(), NOW(), 0),
(23, 2, 180, 0, 30, 600, NOW(), NOW(), 0),
(24, 2, 90, 0, 15, 400, NOW(), NOW(), 0),
(25, 2, 180, 0, 30, 600, NOW(), NOW(), 0),
(26, 2, 90, 0, 15, 400, NOW(), NOW(), 0);

-- Super Store Inventory (Shop 3) - 5 variants
INSERT INTO inventory (product_variant_id, shop_id, available_quantity, reserved_quantity, reorder_level, max_stock_level, created_at, updated_at, version)
VALUES 
(27, 3, 100, 0, 15, 500, NOW(), NOW(), 0),
(28, 3, 50, 0, 8, 200, NOW(), NOW(), 0),
(29, 3, 200, 0, 30, 1000, NOW(), NOW(), 0),
(30, 3, 120, 0, 20, 500, NOW(), NOW(), 0),
(31, 3, 40, 0, 8, 150, NOW(), NOW(), 0);

-- Daily Needs Inventory (Shop 4) - 6 variants
INSERT INTO inventory (product_variant_id, shop_id, available_quantity, reserved_quantity, reorder_level, max_stock_level, created_at, updated_at, version)
VALUES 
(32, 4, 150, 0, 25, 600, NOW(), NOW(), 0),
(33, 4, 70, 0, 12, 300, NOW(), NOW(), 0),
(34, 4, 120, 0, 20, 500, NOW(), NOW(), 0),
(35, 4, 60, 0, 10, 300, NOW(), NOW(), 0),
(36, 4, 90, 0, 15, 400, NOW(), NOW(), 0),
(37, 4, 50, 0, 10, 200, NOW(), NOW(), 0);

-- ========================================
-- DATA SUMMARY
-- ========================================
-- ✓ 4 Shops with geospatial coordinates + timestamps
-- ✓ 18 Products across 6 categories + timestamps
-- ✓ 37 Product Variants with proper sizing + timestamps
-- ✓ 37 Inventory records with locking + timestamps
-- ✓ All created_at/updated_at fields populated with NOW()
-- ✓ Ready for production testing
