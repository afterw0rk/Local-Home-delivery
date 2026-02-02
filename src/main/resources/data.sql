-- ========================================
-- Q-Commerce Platform - Sample Data
-- ========================================
-- This file is automatically executed by Spring Boot after schema creation
-- Provides sample shops, products, variants, and inventory for testing

-- ========================================
-- SHOPS (with Geospatial Data)
-- ========================================

-- Shop 1: Fresh Mart (Downtown)
INSERT INTO shops (shop_name, owner_name, phone, email, address, city, state, pincode, location, is_active, operating_hours, delivery_radius_km, created_at, updated_at)
VALUES (
    'Fresh Mart Downtown',
    'Rajesh Kumar',
    '+91-9876543210',
    'rajesh@freshmart.com',
    '123 MG Road',
    'Bangalore',
    'Karnataka',
    '560001',
    ST_GeomFromText('POINT(77.5946 12.9716)', 4326),  -- Bangalore coordinates
    true,
    '08:00-22:00',
    5.0,
    NOW(),
    NOW()
);

-- Shop 2: Quick Grocery (Indiranagar)
INSERT INTO shops (shop_name, owner_name, phone, email, address, city, state, pincode, location, is_active, operating_hours, delivery_radius_km, created_at, updated_at)
VALUES (
    'Quick Grocery Indiranagar',
    'Priya Sharma',
    '+91-9876543211',
    'priya@quickgrocery.com',
    '456 CMH Road',
    'Bangalore',
    'Karnataka',
    '560038',
    ST_GeomFromText('POINT(77.6412 12.9784)', 4326),  -- Indiranagar coordinates
    true,
    '07:00-23:00',
    3.0,
    NOW(),
    NOW()
);

-- Shop 3: Super Store (Koramangala)
INSERT INTO shops (shop_name, owner_name, phone, email, address, city, state, pincode, location, is_active, operating_hours, delivery_radius_km, created_at, updated_at)
VALUES (
    'Super Store Koramangala',
    'Amit Patel',
    '+91-9876543212',
    'amit@superstore.com',
    '789 80 Feet Road',
    'Bangalore',
    'Karnataka',
    '560034',
    ST_GeomFromText('POINT(77.6175 12.9352)', 4326),  -- Koramangala coordinates
    true,
    '06:00-23:30',
    4.0,
    NOW(),
    NOW()
);

-- Shop 4: Daily Needs (Whitefield)
INSERT INTO shops (shop_name, owner_name, phone, email, address, city, state, pincode, location, is_active, operating_hours, delivery_radius_km, created_at, updated_at)
VALUES (
    'Daily Needs Whitefield',
    'Sneha Reddy',
    '+91-9876543213',
    'sneha@dailyneeds.com',
    '321 ITPL Main Road',
    'Bangalore',
    'Karnataka',
    '560066',
    ST_GeomFromText('POINT(77.7500 12.9698)', 4326),  -- Whitefield coordinates
    true,
    '08:00-22:00',
    6.0,
    NOW(),
    NOW()
);

-- ========================================
-- PRODUCTS
-- ========================================

-- Dairy Products (Shop 1)
INSERT INTO products (product_name, description, category_id, shop_id, image_url, is_active, created_at, updated_at)
VALUES 
('Amul Fresh Milk', 'Fresh full cream milk', 1, 1, 'https://images.example.com/milk.jpg', true, NOW(), NOW()),
('Amul Butter', 'Utterly butterly delicious', 1, 1, 'https://images.example.com/butter.jpg', true, NOW(), NOW()),
('Nestle Yogurt', 'Creamy yogurt', 1, 1, 'https://images.example.com/yogurt.jpg', true, NOW(), NOW());

-- Fruits & Vegetables (Shop 1)
INSERT INTO products (product_name, description, category_id, shop_id, image_url, is_active, created_at, updated_at)
VALUES 
('Fresh Banana', 'Organic bananas from local farms', 2, 1, 'https://images.example.com/banana.jpg', true, NOW(), NOW()),
('Red Apple', 'Crispy Shimla apples', 2, 1, 'https://images.example.com/apple.jpg', true, NOW(), NOW()),
('Fresh Tomato', 'Farm fresh tomatoes', 2, 1, 'https://images.example.com/tomato.jpg', true, NOW(), NOW());

-- Snacks (Shop 2)
INSERT INTO products (product_name, description, category_id, shop_id, image_url, is_active, created_at, updated_at)
VALUES 
('Lays Chips', 'Crispy potato chips', 3, 2, 'https://images.example.com/lays.jpg', true, NOW(), NOW()),
('Parle-G Biscuits', 'Classic glucose biscuits', 3, 2, 'https://images.example.com/parleg.jpg', true, NOW(), NOW()),
('Maggi Noodles', '2-minute instant noodles', 3, 2, 'https://images.example.com/maggi.jpg', true, NOW(), NOW());

-- Beverages (Shop 2)
INSERT INTO products (product_name, description, category_id, shop_id, image_url, is_active, created_at, updated_at)
VALUES 
('Coca Cola', 'Classic cola drink', 4, 2, 'https://images.example.com/coke.jpg', true, NOW(), NOW()),
('Pepsi', 'Refreshing cola', 4, 2, 'https://images.example.com/pepsi.jpg', true, NOW(), NOW()),
('Sprite', 'Lemon-lime drink', 4, 2, 'https://images.example.com/sprite.jpg', true, NOW(), NOW());

-- Staples (Shop 3)
INSERT INTO products (product_name, description, category_id, shop_id, image_url, is_active, created_at, updated_at)
VALUES 
('India Gate Basmati Rice', 'Premium basmati rice', 5, 3, 'https://images.example.com/rice.jpg', true, NOW(), NOW()),
('Tata Salt', 'Iodized table salt', 5, 3, 'https://images.example.com/salt.jpg', true, NOW(), NOW()),
('Fortune Sunflower Oil', 'Refined sunflower oil', 5, 3, 'https://images.example.com/oil.jpg', true, NOW(), NOW());

-- Personal Care (Shop 4)
INSERT INTO products (product_name, description, category_id, shop_id, image_url, is_active, created_at, updated_at)
VALUES 
('Dove Soap', 'Moisturizing beauty soap', 6, 4, 'https://images.example.com/dove.jpg', true, NOW(), NOW()),
('Colgate Toothpaste', 'Total advanced health', 6, 4, 'https://images.example.com/colgate.jpg', true, NOW(), NOW()),
('Pantene Shampoo', 'Pro-V hair fall control', 6, 4, 'https://images.example.com/pantene.jpg', true, NOW(), NOW());

-- ========================================
-- PRODUCT VARIANTS
-- ========================================

-- Milk Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(1, '500ml Pouch', '500ml', NULL, 25.00, 'MILK-500ML', 'BAR001', true, NOW(), NOW()),
(1, '1 Liter Pouch', '1L', NULL, 48.00, 'MILK-1L', 'BAR002', true, NOW(), NOW());

-- Butter Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(2, '100g Pack', NULL, '100g', 52.00, 'BUTTER-100G', 'BAR003', true, NOW(), NOW()),
(2, '500g Pack', NULL, '500g', 250.00, 'BUTTER-500G', 'BAR004', true, NOW(), NOW());

-- Yogurt Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(3, '200g Cup', NULL, '200g', 30.00, 'YOGURT-200G', 'BAR005', true, NOW(), NOW()),
(3, '400g Cup', NULL, '400g', 55.00, 'YOGURT-400G', 'BAR006', true, NOW(), NOW());

-- Banana Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(4, '1 Dozen (12 pcs)', NULL, '1.5kg', 60.00, 'BANANA-12PC', 'BAR007', true, NOW(), NOW()),
(4, '6 Pieces', NULL, '750g', 35.00, 'BANANA-6PC', 'BAR008', true, NOW(), NOW());

-- Apple Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(5, '1kg Pack', NULL, '1kg', 180.00, 'APPLE-1KG', 'BAR009', true, NOW(), NOW()),
(5, '500g Pack', NULL, '500g', 95.00, 'APPLE-500G', 'BAR010', true, NOW(), NOW());

-- Tomato Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(6, '1kg Pack', NULL, '1kg', 40.00, 'TOMATO-1KG', 'BAR011', true, NOW(), NOW()),
(6, '500g Pack', NULL, '500g', 22.00, 'TOMATO-500G', 'BAR012', true, NOW(), NOW());

-- Lays Chips Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(7, 'Small Pack', NULL, '25g', 10.00, 'LAYS-25G', 'BAR013', true, NOW(), NOW()),
(7, 'Medium Pack', NULL, '52g', 20.00, 'LAYS-52G', 'BAR014', true, NOW(), NOW()),
(7, 'Large Pack', NULL, '90g', 35.00, 'LAYS-90G', 'BAR015', true, NOW(), NOW());

-- Parle-G Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(8, 'Small Pack', NULL, '56g', 5.00, 'PARLEG-56G', 'BAR016', true, NOW(), NOW()),
(8, 'Family Pack', NULL, '376g', 30.00, 'PARLEG-376G', 'BAR017', true, NOW(), NOW());

-- Maggi Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(9, 'Single Pack', NULL, '70g', 14.00, 'MAGGI-70G', 'BAR018', true, NOW(), NOW()),
(9, '4-Pack', NULL, '280g', 52.00, 'MAGGI-280G', 'BAR019', true, NOW(), NOW());

-- Coca Cola Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(10, '250ml Bottle', '250ml', NULL, 20.00, 'COKE-250ML', 'BAR020', true, NOW(), NOW()),
(10, '750ml Bottle', '750ml', NULL, 40.00, 'COKE-750ML', 'BAR021', true, NOW(), NOW()),
(10, '2L Bottle', '2L', NULL, 90.00, 'COKE-2L', 'BAR022', true, NOW(), NOW());

-- Pepsi Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(11, '250ml Bottle', '250ml', NULL, 20.00, 'PEPSI-250ML', 'BAR023', true, NOW(), NOW()),
(11, '750ml Bottle', '750ml', NULL, 40.00, 'PEPSI-750ML', 'BAR024', true, NOW(), NOW());

-- Sprite Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(12, '250ml Bottle', '250ml', NULL, 20.00, 'SPRITE-250ML', 'BAR025', true, NOW(), NOW()),
(12, '750ml Bottle', '750ml', NULL, 40.00, 'SPRITE-750ML', 'BAR026', true, NOW(), NOW());

-- Basmati Rice Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(13, '1kg Pack', NULL, '1kg', 120.00, 'RICE-1KG', 'BAR027', true, NOW(), NOW()),
(13, '5kg Bag', NULL, '5kg', 550.00, 'RICE-5KG', 'BAR028', true, NOW(), NOW());

-- Salt Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(14, '1kg Pack', NULL, '1kg', 20.00, 'SALT-1KG', 'BAR029', true, NOW(), NOW());

-- Oil Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(15, '1 Liter Bottle', '1L', NULL, 150.00, 'OIL-1L', 'BAR030', true, NOW(), NOW()),
(15, '5 Liter Can', '5L', NULL, 700.00, 'OIL-5L', 'BAR031', true, NOW(), NOW());

-- Dove Soap Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(16, 'Single Bar', NULL, '100g', 45.00, 'DOVE-100G', 'BAR032', true, NOW(), NOW()),
(16, '3-Pack', NULL, '300g', 120.00, 'DOVE-300G', 'BAR033', true, NOW(), NOW());

-- Colgate Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(17, '100g Tube', NULL, '100g', 85.00, 'COLGATE-100G', 'BAR034', true, NOW(), NOW()),
(17, '200g Tube', NULL, '200g', 155.00, 'COLGATE-200G', 'BAR035', true, NOW(), NOW());

-- Pantene Variants
INSERT INTO product_variants (product_id, variant_name, size, weight, price, sku, barcode, is_active, created_at, updated_at)
VALUES 
(18, '180ml Bottle', '180ml', NULL, 175.00, 'PANTENE-180ML', 'BAR036', true, NOW(), NOW()),
(18, '340ml Bottle', '340ml', NULL, 320.00, 'PANTENE-340ML', 'BAR037', true, NOW(), NOW());

-- ========================================
-- INVENTORY (with Optimistic Locking)
-- ========================================

-- Fresh Mart Inventory (Shop 1)
INSERT INTO inventory (product_variant_id, shop_id, available_quantity, reserved_quantity, reorder_level, reorder_quantity, version, created_at, updated_at)
VALUES 
(1, 1, 150, 0, 20, 100, 0, NOW(), NOW()),   -- Milk 500ml
(2, 1, 80, 0, 15, 50, 0, NOW(), NOW()),     -- Milk 1L
(3, 1, 100, 0, 10, 60, 0, NOW(), NOW()),    -- Butter 100g
(4, 1, 45, 0, 5, 30, 0, NOW(), NOW()),      -- Butter 500g
(5, 1, 120, 0, 15, 80, 0, NOW(), NOW()),    -- Yogurt 200g
(6, 1, 60, 0, 10, 40, 0, NOW(), NOW()),     -- Yogurt 400g
(7, 1, 200, 0, 30, 150, 0, NOW(), NOW()),   -- Banana 12pc
(8, 1, 180, 0, 25, 120, 0, NOW(), NOW()),   -- Banana 6pc
(9, 1, 90, 0, 15, 60, 0, NOW(), NOW()),     -- Apple 1kg
(10, 1, 140, 0, 20, 100, 0, NOW(), NOW()),  -- Apple 500g
(11, 1, 250, 0, 40, 200, 0, NOW(), NOW()),  -- Tomato 1kg
(12, 1, 200, 0, 30, 150, 0, NOW(), NOW());  -- Tomato 500g

-- Quick Grocery Inventory (Shop 2)
INSERT INTO inventory (product_variant_id, shop_id, available_quantity, reserved_quantity, reorder_level, reorder_quantity, version, created_at, updated_at)
VALUES 
(13, 2, 300, 0, 50, 250, 0, NOW(), NOW()),  -- Lays 25g
(14, 2, 200, 0, 30, 150, 0, NOW(), NOW()),  -- Lays 52g
(15, 2, 120, 0, 20, 80, 0, NOW(), NOW()),   -- Lays 90g
(16, 2, 500, 0, 80, 400, 0, NOW(), NOW()),  -- Parle-G 56g
(17, 2, 150, 0, 25, 100, 0, NOW(), NOW()),  -- Parle-G 376g
(18, 2, 250, 0, 40, 200, 0, NOW(), NOW()),  -- Maggi 70g
(19, 2, 100, 0, 15, 70, 0, NOW(), NOW()),   -- Maggi 280g
(20, 2, 180, 0, 30, 120, 0, NOW(), NOW()),  -- Coke 250ml
(21, 2, 90, 0, 15, 60, 0, NOW(), NOW()),    -- Coke 750ml
(22, 2, 50, 0, 10, 40, 0, NOW(), NOW()),    -- Coke 2L
(23, 2, 180, 0, 30, 120, 0, NOW(), NOW()),  -- Pepsi 250ml
(24, 2, 90, 0, 15, 60, 0, NOW(), NOW()),    -- Pepsi 750ml
(25, 2, 180, 0, 30, 120, 0, NOW(), NOW()),  -- Sprite 250ml
(26, 2, 90, 0, 15, 60, 0, NOW(), NOW());    -- Sprite 750ml

-- Super Store Inventory (Shop 3)
INSERT INTO inventory (product_variant_id, shop_id, available_quantity, reserved_quantity, reorder_level, reorder_quantity, version, created_at, updated_at)
VALUES 
(27, 3, 100, 0, 15, 70, 0, NOW(), NOW()),   -- Rice 1kg
(28, 3, 50, 0, 8, 35, 0, NOW(), NOW()),     -- Rice 5kg
(29, 3, 200, 0, 30, 150, 0, NOW(), NOW()),  -- Salt 1kg
(30, 3, 120, 0, 20, 80, 0, NOW(), NOW()),   -- Oil 1L
(31, 3, 40, 0, 8, 30, 0, NOW(), NOW());     -- Oil 5L

-- Daily Needs Inventory (Shop 4)
INSERT INTO inventory (product_variant_id, shop_id, available_quantity, reserved_quantity, reorder_level, reorder_quantity, version, created_at, updated_at)
VALUES 
(32, 4, 150, 0, 25, 100, 0, NOW(), NOW()),  -- Dove Single
(33, 4, 70, 0, 12, 50, 0, NOW(), NOW()),    -- Dove 3-Pack
(34, 4, 120, 0, 20, 80, 0, NOW(), NOW()),   -- Colgate 100g
(35, 4, 60, 0, 10, 45, 0, NOW(), NOW()),    -- Colgate 200g
(36, 4, 90, 0, 15, 60, 0, NOW(), NOW()),    -- Pantene 180ml
(37, 4, 50, 0, 10, 35, 0, NOW(), NOW());    -- Pantene 340ml

-- ========================================
-- SUMMARY
-- ========================================
-- 4 Shops across different locations in Bangalore
-- 18 Products across multiple categories
-- 37 Product Variants with different sizes and weights
-- Full inventory setup with optimistic locking support
-- Ready for testing geospatial queries, inventory management, and order processing
