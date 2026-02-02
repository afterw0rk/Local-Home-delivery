# Automatic Database Setup Guide

## Overview

The Q-Commerce platform now handles **automatic database schema creation** - no manual SQL scripts needed!

## How It Works

### 1. Hibernate Auto DDL (JPA)
```properties
spring.jpa.hibernate.ddl-auto=update
```

- **Automatically creates tables** from your `@Entity` classes on startup
- **Adds new columns** when you add fields to entities
- **Safe**: Never drops existing tables or data
- **Smart**: Detects schema changes and applies them

### 2. Schema Initializer Component

The `DatabaseSchemaInitializer` runs after Hibernate and creates:

- **Spatial Indexes** on `shops.location` for geolocation queries
- **Performance Indexes** on frequently queried columns
- **Unique Constraints** to prevent duplicate data
- **Composite Indexes** for complex queries

### 3. Sample Data Loader (Optional)

When running in `dev` profile, automatically loads:
- Sample shop with Mumbai location
- Sample products and variants
- Sample inventory records

## Quick Start

### Step 1: Create MySQL Database

```sql
CREATE DATABASE qcommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

That's it! Just create an empty database. The application will handle everything else.

### Step 2: Configure Connection

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/qcommerce_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Step 3: Run the Application

```bash
mvn spring-boot:run
```

**First Run Output:**
```
[v0] Starting automatic database schema initialization...
[v0] Creating spatial indexes...
[v0] Creating performance indexes...
[v0] Creating unique constraints...
[v0] Database schema initialized successfully!
```

### Step 4: Load Sample Data (Optional)

To load sample data for testing:

```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

## What Gets Created Automatically

### Tables (from JPA Entities)
- ✅ `shops` - With POINT spatial column
- ✅ `products` - Product catalog
- ✅ `product_variants` - Size/weight variants
- ✅ `inventory` - Stock levels with optimistic locking
- ✅ `carts` - Shopping carts
- ✅ `cart_items` - Cart line items
- ✅ `orders` - Order records
- ✅ `order_items` - Order line items

### Spatial Indexes
- ✅ `idx_shop_location` on `shops(location)` - For ST_Distance queries

### Performance Indexes
- ✅ `idx_product_shop_id` - Filter products by shop
- ✅ `idx_inventory_variant_id` - Fast inventory lookups
- ✅ `idx_inventory_shop_variant` - Composite for shop + variant
- ✅ `idx_order_user_id` - User order history
- ✅ `idx_order_status` - Filter orders by status
- ✅ `idx_cart_user_id` - Fast cart retrieval

### Unique Constraints
- ✅ `uk_shop_owner_email` - Prevent duplicate shop registrations
- ✅ `uk_shop_phone` - Unique phone numbers
- ✅ `uk_inventory_shop_variant` - One inventory record per shop+variant
- ✅ `uk_cart_item` - One item per variant in cart

## Schema Updates

### Adding a New Field

Just add it to your entity:

```java
@Entity
public class Shop {
    // ...existing fields
    
    @Column(name = "rating")
    private Double rating;  // NEW FIELD
}
```

**Next startup:** Hibernate automatically adds the `rating` column!

### Adding a New Table

Create a new entity:

```java
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
}
```

**Next startup:** Table `categories` is automatically created!

## Production Considerations

### For Production Environments

1. **Change DDL mode to validate:**
```properties
spring.jpa.hibernate.ddl-auto=validate
```

2. **Use Flyway for controlled migrations:**
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

3. **Keep schema initializer** for indexes (they're idempotent)

### Safety Features

- **Idempotent Operations**: Schema initializer checks if indexes exist before creating
- **Non-Destructive**: `ddl-auto=update` never drops tables or data
- **Error Handling**: Gracefully handles already-existing indexes
- **Logging**: Detailed logs show what was created/skipped

## Troubleshooting

### Issue: Tables not created

**Solution:** Check Hibernate is scanning your entities:
```properties
spring.jpa.show-sql=true
```

### Issue: Spatial index failed

**Solution:** Ensure MySQL has spatial support:
```sql
SHOW VARIABLES LIKE 'version';  -- Should be 8.0+
```

### Issue: Duplicate key errors

**Solution:** Unique constraints are working! Check your data for duplicates.

## Comparison: Manual vs Automatic

| Aspect | Manual SQL Scripts | Automatic (Current) |
|--------|-------------------|-------------------|
| Setup Time | 30+ minutes | 2 minutes |
| New Columns | Write ALTER TABLE | Just add field |
| New Tables | Write CREATE TABLE | Just add entity |
| Indexes | Remember to add | Automatic |
| Environment Sync | Manual work | Automatic |
| Developer Experience | ⭐⭐ | ⭐⭐⭐⭐⭐ |

## Advanced: Custom Indexes

To add your own indexes, update `DatabaseSchemaInitializer`:

```java
executeIfNotExists(
    "SELECT COUNT(*) FROM information_schema.statistics " +
    "WHERE table_schema = DATABASE() AND table_name = 'your_table' AND index_name = 'idx_your_index'",
    "CREATE INDEX idx_your_index ON your_table(your_column)"
);
```

## Verification

After startup, verify schema:

```sql
-- Check tables
SHOW TABLES;

-- Check spatial index
SHOW INDEX FROM shops WHERE Key_name = 'idx_shop_location';

-- Check constraints
SELECT * FROM information_schema.table_constraints 
WHERE table_schema = 'qcommerce_db';

-- Test spatial query
SELECT shop_name, 
       ST_Distance_Sphere(location, ST_GeomFromText('POINT(72.8777 19.0760)')) as distance_meters
FROM shops
WHERE ST_Distance_Sphere(location, ST_GeomFromText('POINT(72.8777 19.0760)')) < 2000
ORDER BY distance_meters;
```

## Summary

You now have a **zero-configuration database setup**:
1. Create empty database
2. Run application
3. Everything is ready!

No SQL scripts, no migrations, no hassle. Just pure Spring Boot magic! ✨
