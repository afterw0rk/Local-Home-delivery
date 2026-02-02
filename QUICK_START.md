# Quick Commerce Platform - Quick Start Guide

## Automated Setup (Zero Manual SQL)

The application now uses **automatic schema creation and data population**. You don't need to run any SQL scripts manually.

---

## Prerequisites

1. **Java 21** installed
2. **MySQL 8.0+** installed and running
3. **Redis** installed and running
4. **Maven** installed

---

## Step 1: Create Empty Database

```sql
CREATE DATABASE qcommerce_db;
```

That's it! No need to create tables manually.

---

## Step 2: Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/qcommerce_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

---

## Step 3: Start Redis

```bash
# Linux/Mac
redis-server

# Windows
redis-server.exe

# Docker
docker run -d -p 6379:6379 redis:latest
```

---

## Step 4: Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

### What Happens Automatically:

1. **Hibernate creates all tables** from JPA entities (shops, products, variants, inventory, orders, carts, etc.)
2. **DatabaseSchemaInitializer** adds spatial indexes and performance indexes
3. **data.sql executes** and populates:
   - 4 shops across Bangalore with real geospatial coordinates
   - 18 products across multiple categories
   - 37 product variants with different sizes
   - Full inventory setup with optimistic locking

---

## Step 5: Verify Setup

### Check Application Logs

You should see:
```
[v0] Checking if spatial index already exists: idx_shop_location
[v0] Creating spatial index: idx_shop_location
[v0] All database indexes and constraints created successfully!
```

### Test the API

```bash
# Search nearby products (2km radius around Bangalore downtown)
curl -X POST http://localhost:8080/api/products/search/nearby \
-H "Content-Type: application/json" \
-d '{
  "latitude": 12.9716,
  "longitude": 77.5946,
  "radiusKm": 2.0,
  "categoryId": null,
  "page": 0,
  "size": 20
}'
```

### Access Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## Sample Data Loaded

### Shops (with Geospatial Data)

1. **Fresh Mart Downtown** - MG Road, Bangalore (77.5946, 12.9716)
2. **Quick Grocery Indiranagar** - CMH Road (77.6412, 12.9784)
3. **Super Store Koramangala** - 80 Feet Road (77.6175, 12.9352)
4. **Daily Needs Whitefield** - ITPL Main Road (77.7500, 12.9698)

### Products

- **Dairy**: Milk, Butter, Yogurt
- **Fruits & Vegetables**: Banana, Apple, Tomato
- **Snacks**: Lays Chips, Parle-G, Maggi Noodles
- **Beverages**: Coca Cola, Pepsi, Sprite
- **Staples**: Basmati Rice, Salt, Sunflower Oil
- **Personal Care**: Dove Soap, Colgate, Pantene Shampoo

---

## Key API Endpoints

### Shop Onboarding
```bash
POST /api/shops/register
GET /api/shops/{id}
PUT /api/shops/{id}
```

### Product Search
```bash
POST /api/products/search/nearby
GET /api/products/search/by-shop/{shopId}
```

### Cart Management
```bash
POST /api/cart/add        # Add item to cart
GET /api/cart             # Get active cart
PUT /api/cart/item/{itemId}  # Update quantity
DELETE /api/cart/item/{itemId}  # Remove item
DELETE /api/cart/clear    # Clear cart
```

### Order Processing
```bash
POST /api/orders/place    # Place order from cart
GET /api/orders/{id}      # Get order details
GET /api/orders/user      # Get user orders
POST /api/orders/{id}/cancel  # Cancel order
```

### WebSocket (Real-time Order Updates)
```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    // Subscribe to order updates
    stompClient.subscribe('/topic/orders/123', (message) => {
        console.log('Order update:', JSON.parse(message.body));
    });
});
```

---

## Testing Optimistic Locking (Flash Sale Scenario)

```bash
# Simulate 100 concurrent users trying to buy the last 10 units
# The optimistic locking will prevent overselling

# Terminal 1
curl -X POST http://localhost:8080/api/orders/place \
-H "Content-Type: application/json" \
-H "X-User-Id: user-1" \
-d '{"deliveryAddressId": 1}'

# Terminal 2 (at the same time)
curl -X POST http://localhost:8080/api/orders/place \
-H "Content-Type: application/json" \
-H "X-User-Id: user-2" \
-d '{"deliveryAddressId": 1}'

# Spring Retry will automatically retry up to 5 times with exponential backoff
```

---

## Configuration Tips

### For Production

```properties
# Disable data.sql auto-execution in production
spring.sql.init.mode=never

# Use connection pooling
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# Set proper cache TTL
spring.cache.redis.time-to-live=300000  # 5 minutes
```

### For Development

```properties
# See all SQL queries
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG

# Re-populate data on every restart
spring.jpa.hibernate.ddl-auto=create-drop
spring.sql.init.mode=always
```

---

## Troubleshooting

### Error: "Table doesn't exist"
**Solution**: Ensure `spring.jpa.hibernate.ddl-auto=update` is set in application.properties

### Error: "Spatial index failed"
**Solution**: The DatabaseSchemaInitializer checks if indexes exist before creating them. Check logs for details.

### Error: "Connection refused"
**Solution**: Ensure MySQL and Redis are running:
```bash
# Check MySQL
mysqladmin ping

# Check Redis
redis-cli ping
```

### Error: "Data already exists"
**Solution**: If you're re-running the app and data.sql fails due to duplicate keys, either:
- Drop and recreate the database
- Set `spring.sql.init.mode=never` after first run

---

## Next Steps

1. Review the [Architecture Documentation](docs/ARCHITECTURE.md)
2. Check [API Testing Guide](docs/API_TESTING_GUIDE.md) for detailed examples
3. Explore [Database ERD](docs/DATABASE_ERD.md) for schema details
4. Read [Compilation Checklist](COMPILATION_CHECKLIST.md) for development tips

---

**Your Q-Commerce platform is now ready to compete with Zepto and Instamart!** 🚀
