# 🧪 API Testing Guide - Quick Commerce Platform

## 📝 Table of Contents
1. [Setup](#setup)
2. [Shop Management APIs](#shop-management-apis)
3. [Product Search APIs](#product-search-apis)
4. [Order Management APIs](#order-management-apis)
5. [WebSocket Testing](#websocket-testing)
6. [Performance Testing](#performance-testing)

---

## 🚀 Setup

### Start Services

```bash
# 1. Start MySQL
mysql -u root -p
source src/main/resources/db/migration/V1__Initial_Schema.sql

# 2. Start Redis
redis-server

# 3. Start Application
mvn spring-boot:run

# Application runs on: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

---

## 🏪 Shop Management APIs

### 1. Register New Shop (Vendor Onboarding)

**Endpoint:** `POST /api/v1/shops/register`

```bash
curl -X POST http://localhost:8080/api/v1/shops/register \
  -H "Content-Type: application/json" \
  -d '{
    "shopName": "Quick Mart Express",
    "ownerName": "Rajesh Kumar",
    "email": "rajesh@quickmart.com",
    "phoneNumber": "+919876543210",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "addressLine1": "123, MG Road",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "gstNumber": "29ABCDE1234F1Z5",
    "fssaiLicense": "12345678901234",
    "deliveryRadiusKm": 3.0,
    "minOrderAmount": 100.00,
    "openingTime": "08:00:00",
    "closingTime": "22:00:00"
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "shopName": "Quick Mart Express",
  "ownerName": "Rajesh Kumar",
  "email": "rajesh@quickmart.com",
  "shopStatus": "PENDING",
  "latitude": 12.9716,
  "longitude": 77.5946,
  "deliveryRadiusKm": 3.0,
  "isOperational": false,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

### 2. Approve Shop (Admin)

**Endpoint:** `PUT /api/v1/shops/{shopId}/status?status=APPROVED`

```bash
curl -X PUT "http://localhost:8080/api/v1/shops/1/status?status=APPROVED"
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "shopStatus": "APPROVED",
  "isOperational": true
}
```

---

### 3. Get Shop Details

**Endpoint:** `GET /api/v1/shops/{shopId}`

```bash
curl -X GET http://localhost:8080/api/v1/shops/1
```

---

### 4. Find Nearby Shops

**Endpoint:** `GET /api/v1/shops/nearby`

```bash
curl -X GET "http://localhost:8080/api/v1/shops/nearby?latitude=12.9716&longitude=77.5946&radiusKm=5.0&page=0&pageSize=20"
```

**Expected Response:**
```json
{
  "content": [
    {
      "id": 1,
      "shopName": "Quick Mart Express",
      "distanceKm": 1.2,
      "deliveryRadiusKm": 3.0,
      "isOperational": true
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

---

## 🔍 Product Search APIs

### 1. Search Nearby Products (POST)

**Endpoint:** `POST /api/v1/products/search/nearby`

```bash
curl -X POST http://localhost:8080/api/v1/products/search/nearby \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 12.9716,
    "longitude": 77.5946,
    "radiusKm": 2.0,
    "categoryId": 1,
    "searchKeyword": "milk",
    "page": 0,
    "pageSize": 20,
    "sortBy": "DISTANCE"
  }'
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "productId": 123,
      "productName": "Amul Gold Milk",
      "brand": "Amul",
      "primaryImageUrl": "https://example.com/images/milk.jpg",
      "hasVariants": true,
      "variants": [
        {
          "variantId": 456,
          "variantName": "500ml",
          "sku": "AMUL-MILK-500ML",
          "price": 28.00,
          "mrp": 30.00,
          "availableQuantity": 50,
          "weightDisplay": "500ml"
        },
        {
          "variantId": 457,
          "variantName": "1L",
          "sku": "AMUL-MILK-1L",
          "price": 54.00,
          "mrp": 58.00,
          "availableQuantity": 30,
          "weightDisplay": "1l"
        }
      ],
      "shop": {
        "shopId": 1,
        "shopName": "Quick Mart Express",
        "address": "123, MG Road, Bangalore",
        "distanceKm": 1.2,
        "minOrderAmount": 100.00,
        "isOpen": true,
        "estimatedDeliveryTime": "15-20 mins"
      },
      "distanceKm": 1.2,
      "inStock": true
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

**Response Time Benchmarks:**
- ⏱️ Cold (no cache): 80-150ms
- ⏱️ Warm (Redis cache): **10-20ms** ✅

---

### 2. Search Nearby Products (GET - Simplified)

**Endpoint:** `GET /api/v1/products/nearby`

```bash
curl -X GET "http://localhost:8080/api/v1/products/nearby?latitude=12.9716&longitude=77.5946&radiusKm=2.0&searchKeyword=milk&page=0&pageSize=20&sortBy=DISTANCE"
```

---

### 3. Check Product Availability

**Endpoint:** `GET /api/v1/products/{productId}/availability`

```bash
curl -X GET "http://localhost:8080/api/v1/products/123/availability?latitude=12.9716&longitude=77.5946&radiusKm=2.0"
```

**Expected Response:**
```json
true
```

---

## 🛒 Order Management APIs

### 1. Place Order (with Optimistic Locking)

**Endpoint:** `POST /api/v1/orders`

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "shopId": 1,
    "items": [
      {
        "productVariantId": 456,
        "quantity": 2
      },
      {
        "productVariantId": 789,
        "quantity": 1
      }
    ],
    "deliveryAddressId": 1,
    "paymentMethod": "UPI"
  }'
```

**Expected Response (201 Created):**
```json
{
  "orderId": 12345,
  "orderNumber": "ORD-2024-001",
  "orderStatus": "PAYMENT_PENDING",
  "totalAmount": 156.00,
  "estimatedDeliveryTime": "2024-01-15T11:00:00Z",
  "message": "Order placed successfully!"
}
```

**Error Response (Insufficient Stock):**
```json
{
  "error": "InsufficientStockException",
  "message": "Only 1 items available",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Error Response (Optimistic Lock Conflict):**
```json
{
  "error": "OptimisticLockException",
  "message": "Out of stock or concurrent update. Please retry.",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

### 2. Get Order Details

**Endpoint:** `GET /api/v1/orders/{orderId}`

```bash
curl -X GET http://localhost:8080/api/v1/orders/12345
```

---

## 🔌 WebSocket Testing

### Using JavaScript (Browser Console)

```javascript
// 1. Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// 2. Subscribe to order updates
stompClient.connect({}, () => {
    console.log('✅ Connected to WebSocket');
    
    // Subscribe to specific order
    stompClient.subscribe('/topic/orders/12345', (message) => {
        const update = JSON.parse(message.body);
        console.log('📦 Order Update:', update);
        
        /*
         * Expected updates:
         * { orderId: 12345, orderStatus: "PAYMENT_CONFIRMED", message: "Payment received", timestamp: "..." }
         * { orderId: 12345, orderStatus: "PREPARING", message: "Shop is preparing your order", timestamp: "..." }
         * { orderId: 12345, orderStatus: "OUT_FOR_DELIVERY", message: "Order is on the way", timestamp: "..." }
         * { orderId: 12345, orderStatus: "DELIVERED", message: "Order delivered successfully", timestamp: "..." }
         */
    });
});

// 3. Disconnect
stompClient.disconnect(() => {
    console.log('❌ Disconnected');
});
```

---

### Using wscat (CLI Tool)

```bash
# Install wscat
npm install -g wscat

# Connect to WebSocket
wscat -c ws://localhost:8080/ws-native

# Send STOMP CONNECT frame
CONNECT
accept-version:1.1,1.0
heart-beat:10000,10000

# Subscribe to order updates
SUBSCRIBE
id:sub-0
destination:/topic/orders/12345

# You'll receive messages like:
MESSAGE
destination:/topic/orders/12345
content-type:application/json

{"orderId":12345,"orderStatus":"OUT_FOR_DELIVERY","message":"Order is on the way"}
```

---

## 📊 Performance Testing

### Using Apache Bench (ab)

#### Test 1: Nearby Product Search

```bash
# 1000 requests, 50 concurrent
ab -n 1000 -c 50 \
   -H "Content-Type: application/json" \
   -p search_payload.json \
   http://localhost:8080/api/v1/products/search/nearby

# search_payload.json:
# {"latitude":12.9716,"longitude":77.5946,"radiusKm":2.0}
```

**Expected Results:**
- **Requests per second:** 4500+ RPS
- **Mean response time:** 20-50ms
- **95th percentile:** <200ms

---

#### Test 2: Shop Registration

```bash
ab -n 100 -c 10 \
   -H "Content-Type: application/json" \
   -p shop_payload.json \
   http://localhost:8080/api/v1/shops/register
```

---

### Using JMeter

#### Load Test Scenario: Flash Sale

```
Thread Group:
  - Users: 5000
  - Ramp-up: 10 seconds
  - Loop: 1

HTTP Request:
  - POST /api/v1/orders
  - 5000 users trying to buy last 100 items

Expected:
  - 100 success (200 OK)
  - 4900 failure (InsufficientStockException or OptimisticLockException)
  - NO negative inventory!
```

---

### Using Artillery

```yaml
# artillery-test.yml
config:
  target: 'http://localhost:8080'
  phases:
    - duration: 60
      arrivalRate: 100
      name: Warm-up
    - duration: 120
      arrivalRate: 500
      name: Load test

scenarios:
  - name: "Product Search"
    flow:
      - post:
          url: "/api/v1/products/search/nearby"
          json:
            latitude: 12.9716
            longitude: 77.5946
            radiusKm: 2.0
```

```bash
artillery run artillery-test.yml
```

---

## 🔐 Testing Optimistic Locking

### Simulate Concurrent Stock Deduction

```bash
# Terminal 1
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"items":[{"productVariantId":456,"quantity":5}]}'

# Terminal 2 (run simultaneously)
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"items":[{"productVariantId":456,"quantity":5}]}'

# Expected:
# - One request succeeds (stock deducted)
# - Other gets OptimisticLockException (retries automatically)
```

---

## 📈 Monitoring

### Check Redis Cache Hits

```bash
# Redis CLI
redis-cli

# Monitor cache operations
MONITOR

# Check cached keys
KEYS nearby_products:*

# Get cache hit rate
INFO stats | grep keyspace
```

---

### Check MySQL Spatial Query Performance

```sql
-- Enable profiling
SET profiling = 1;

-- Run spatial query
SELECT * FROM shops
WHERE ST_Distance_Sphere(location, ST_GeomFromText('POINT(77.5946 12.9716)', 4326)) <= 2000;

-- Check execution time
SHOW PROFILES;

-- Analyze query plan
EXPLAIN SELECT * FROM shops
WHERE ST_Distance_Sphere(location, ST_GeomFromText('POINT(77.5946 12.9716)', 4326)) <= 2000;

-- Expected: Uses idx_shop_location (SPATIAL index)
```

---

## ✅ Test Checklist

- [ ] Shop registration works
- [ ] Shop approval updates status
- [ ] Nearby product search returns results within radius
- [ ] Redis cache speeds up repeated queries
- [ ] Optimistic locking prevents overselling
- [ ] WebSocket broadcasts order status updates
- [ ] Spatial queries use SPATIAL INDEX
- [ ] Load test handles 5000+ RPS
- [ ] Flash sale scenario: No negative inventory

---

## 🐛 Common Issues & Solutions

### Issue 1: "No shops found"
**Cause:** No shops within radius or shops not approved
**Solution:** 
```bash
# Check shop status
curl http://localhost:8080/api/v1/shops/1

# Approve shop
curl -X PUT "http://localhost:8080/api/v1/shops/1/status?status=APPROVED"
```

---

### Issue 2: Slow queries
**Cause:** SPATIAL INDEX not created
**Solution:**
```sql
-- Verify index exists
SHOW INDEX FROM shops WHERE Key_name = 'idx_shop_location';

-- Create if missing
CREATE SPATIAL INDEX idx_shop_location ON shops(location);
```

---

### Issue 3: Cache not working
**Cause:** Redis not running
**Solution:**
```bash
# Check Redis status
redis-cli ping
# Expected: PONG

# Start Redis if needed
redis-server
```

---

## 📚 Further Reading

- [Swagger UI Documentation](http://localhost:8080/swagger-ui.html)
- [Spring Boot Actuator Endpoints](http://localhost:8080/actuator)
- [Redis Cache Metrics](http://localhost:8080/actuator/metrics/cache.gets)

---

**Happy Testing! 🚀**
