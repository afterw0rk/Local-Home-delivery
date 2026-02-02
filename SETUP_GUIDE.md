# Q-Commerce Platform - Complete Setup Guide

## Prerequisites

- **Java 21** (LTS)
- **Maven 3.8+**
- **MySQL 8.0+** (with Spatial support)
- **Redis 6.0+**
- **IDE**: IntelliJ IDEA or VS Code with Java extensions

---

## Step 1: Database Setup

### 1.1 Install MySQL 8.0

```bash
# macOS
brew install mysql@8.0

# Ubuntu
sudo apt-get install mysql-server

# Start MySQL
mysql.server start  # macOS
sudo systemctl start mysql  # Linux
```

### 1.2 Create Database

```sql
CREATE DATABASE qcommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'qcommerce_user'@'localhost' IDENTIFIED BY 'qcommerce_password';
GRANT ALL PRIVILEGES ON qcommerce_db.* TO 'qcommerce_user'@'localhost';
FLUSH PRIVILEGES;
```

### 1.3 Run Migrations

The Flyway migration will automatically create all tables when you start the application for the first time.

To run manually:
```bash
mvn flyway:migrate
```

---

## Step 2: Redis Setup

### 2.1 Install Redis

```bash
# macOS
brew install redis

# Ubuntu
sudo apt-get install redis-server

# Start Redis
redis-server  # Default port 6379
```

### 2.2 Verify Redis

```bash
redis-cli ping
# Should return: PONG
```

---

## Step 3: Application Configuration

### 3.1 Update application.properties

Edit `/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/qcommerce_db
spring.datasource.username=qcommerce_user
spring.datasource.password=qcommerce_password

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Server
server.port=8080
```

### 3.2 Environment Variables (Optional)

For production, use environment variables:

```bash
export DB_URL=jdbc:mysql://prod-db-host:3306/qcommerce_db
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export REDIS_HOST=prod-redis-host
export REDIS_PORT=6379
```

---

## Step 4: Build and Run

### 4.1 Clean Build

```bash
mvn clean install
```

### 4.2 Run Application

```bash
mvn spring-boot:run
```

Or run the JAR:

```bash
java -jar target/quick-commerce-platform-1.0.0.jar
```

### 4.3 Verify Application Started

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

---

## Step 5: Test API Endpoints

### 5.1 Register a Shop

```bash
curl -X POST http://localhost:8080/api/shops \
  -H "Content-Type: application/json" \
  -d '{
    "shopName": "Fresh Mart",
    "ownerName": "John Doe",
    "ownerPhone": "+919876543210",
    "email": "john@freshmart.com",
    "addressLine1": "123 Main Street",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001",
    "latitude": 19.0760,
    "longitude": 72.8777,
    "minOrderAmount": 99.00
  }'
```

### 5.2 Search Nearby Products

```bash
curl -X POST http://localhost:8080/api/products/search/nearby \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 19.0760,
    "longitude": 72.8777,
    "radiusKm": 5.0,
    "page": 0,
    "pageSize": 20,
    "sortBy": "DISTANCE"
  }'
```

### 5.3 Add to Cart

```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "variantId": 1,
    "quantity": 2
  }'
```

### 5.4 Place Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "deliveryAddress": "456 Park Avenue, Mumbai",
    "deliveryLatitude": 19.0760,
    "deliveryLongitude": 72.8777,
    "customerNotes": "Ring doorbell twice"
  }'
```

---

## Step 6: WebSocket Testing

### 6.1 Connect to WebSocket

Use a WebSocket client (e.g., Postman, websocat, or browser):

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to order updates
    stompClient.subscribe('/topic/orders/12345', function(message) {
        console.log('Order update:', JSON.parse(message.body));
    });
});
```

### 6.2 Test Real-Time Updates

Trigger an order status change and watch the WebSocket message:

```bash
curl -X POST http://localhost:8080/api/orders/12345/progress \
  -H "X-User-Id: 1"
```

---

## Step 7: Performance Testing

### 7.1 Test Optimistic Locking (Flash Sale Simulation)

Run multiple concurrent requests to test inventory management:

```bash
# Install Apache Bench
sudo apt-get install apache2-utils

# Simulate 100 concurrent users trying to buy the same product
ab -n 100 -c 50 -T 'application/json' \
   -p order-payload.json \
   http://localhost:8080/api/orders
```

### 7.2 Monitor Redis Cache

```bash
# Monitor Redis commands
redis-cli monitor

# Check cache statistics
redis-cli info stats
```

### 7.3 Check Application Metrics

```bash
# Actuator metrics
curl http://localhost:8080/actuator/metrics

# Database connections
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active

# Cache stats
curl http://localhost:8080/actuator/metrics/cache.gets
```

---

## Step 8: Load Sample Data

### 8.1 Create Sample SQL Script

Create `/src/main/resources/db/data/sample_data.sql`:

```sql
-- Insert sample shop
INSERT INTO shops (shop_name, owner_name, owner_phone, email, address_line1, city, state, pincode, location, min_order_amount, is_operational, created_at, updated_at)
VALUES 
('Fresh Mart', 'John Doe', '+919876543210', 'john@freshmart.com', '123 Main St', 'Mumbai', 'Maharashtra', '400001', 
 ST_GeomFromText('POINT(72.8777 19.0760)', 4326), 99.00, TRUE, NOW(), NOW());

-- Insert sample products
INSERT INTO products (shop_id, category_id, product_name, description, brand, primary_image_url, has_variants, base_price, is_active, created_at, updated_at)
VALUES 
(1, 1, 'Fresh Milk', 'Full cream fresh milk', 'Amul', 'https://example.com/milk.jpg', TRUE, 60.00, TRUE, NOW(), NOW());

-- Insert product variants
INSERT INTO product_variants (product_id, variant_name, sku, price, mrp, weight_value, weight_unit, is_active, created_at, updated_at)
VALUES 
(1, '500ml', 'MILK-500ML', 30.00, 35.00, 500, 'ML', TRUE, NOW(), NOW()),
(1, '1L', 'MILK-1L', 60.00, 65.00, 1, 'L', TRUE, NOW(), NOW());

-- Insert inventory
INSERT INTO inventory (product_variant_id, shop_id, available_quantity, reserved_quantity, reorder_level, last_restocked_at, created_at, updated_at)
VALUES 
(1, 1, 100, 0, 20, NOW(), NOW(), NOW()),
(2, 1, 50, 0, 10, NOW(), NOW(), NOW());
```

### 8.2 Load Sample Data

```bash
mysql -u qcommerce_user -p qcommerce_db < src/main/resources/db/data/sample_data.sql
```

---

## Step 9: IDE Setup

### 9.1 IntelliJ IDEA

1. **Open Project**: File → Open → Select project root directory
2. **Configure JDK**: File → Project Structure → Project SDK → Java 21
3. **Enable Annotation Processing**: Settings → Build → Compiler → Annotation Processors → Enable
4. **Install Plugins**:
   - Lombok Plugin
   - Spring Boot Assistant

### 9.2 VS Code

1. **Install Extensions**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **Configure settings.json**:
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic"
}
```

---

## Step 10: Common Issues and Solutions

### Issue 1: MySQL Spatial Functions Not Working

**Error**: `FUNCTION ST_GeomFromText does not exist`

**Solution**:
```sql
-- Verify MySQL version
SELECT VERSION();  -- Should be 8.0+

-- Check spatial support
SHOW VARIABLES LIKE 'have_geometry';  -- Should be YES
```

### Issue 2: Redis Connection Failed

**Error**: `Unable to connect to Redis`

**Solution**:
```bash
# Check if Redis is running
redis-cli ping

# Check Redis port
netstat -an | grep 6379

# Restart Redis
redis-server --port 6379
```

### Issue 3: Optimistic Locking Failures

**Error**: `ObjectOptimisticLockingFailureException`

**Solution**: This is expected behavior during high concurrency. The `@Retryable` annotation will automatically retry up to 5 times. If you still see errors:
- Increase `maxAttempts` in `@Retryable`
- Use atomic queries (`deductStockAtomic`) instead of entity-based updates

### Issue 4: Slow Spatial Queries

**Problem**: Nearby shop search is slow

**Solution**:
```sql
-- Verify spatial index exists
SHOW INDEX FROM shops WHERE Key_name = 'idx_shop_location';

-- If missing, create it
CREATE SPATIAL INDEX idx_shop_location ON shops(location);

-- Analyze query performance
EXPLAIN SELECT * FROM shops WHERE ST_Distance_Sphere(location, ST_GeomFromText('POINT(72.8777 19.0760)', 4326)) <= 5000;
```

---

## Architecture Verification

### Verify Hexagonal Architecture Layers

```
✅ Domain Layer (Entities)
   - Located in: com.qcommerce.domain.entities
   - Pure business logic, no framework dependencies

✅ Application Layer (Use Cases)
   - Located in: com.qcommerce.application.services
   - Orchestrates domain entities, implements business rules

✅ Infrastructure Layer (Adapters)
   - Located in: com.qcommerce.infrastructure
   - Database repositories, external services, configurations

✅ Interface Layer (Controllers)
   - Located in: com.qcommerce.interfaces
   - REST controllers, DTOs, API contracts
```

---

## Production Checklist

Before deploying to production:

- [ ] Enable Spring Security (currently header-based auth)
- [ ] Add JWT token authentication
- [ ] Configure SSL/TLS certificates
- [ ] Set up database connection pooling (already configured with HikariCP)
- [ ] Configure Redis Sentinel for high availability
- [ ] Add rate limiting for APIs
- [ ] Set up monitoring (Prometheus + Grafana)
- [ ] Configure log aggregation (ELK stack)
- [ ] Enable database backups
- [ ] Set up CI/CD pipeline
- [ ] Configure API documentation (Swagger/OpenAPI already included)
- [ ] Add comprehensive error logging
- [ ] Set up alerting for critical errors

---

## Support

For issues or questions:
- Check `/docs/ARCHITECTURE.md` for design decisions
- Check `/docs/API_TESTING_GUIDE.md` for API examples
- Check `/COMPILATION_FIXES.md` for technical details
- Check application logs: `tail -f logs/application.log`

---

**Built with**: Spring Boot 3.2 | Java 21 | MySQL 8.0 | Redis | Hexagonal Architecture
