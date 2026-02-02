# Q-Commerce Platform Architecture

## Overview

This Q-Commerce platform is built using **Hexagonal Architecture (Ports and Adapters)** to ensure clean separation of concerns, testability, and maintainability. The system is designed to compete with industry leaders like Zepto and Instamart by implementing enterprise-grade patterns.

---

## 🏗️ Architecture Layers

### 1. **Domain Layer** (Core Business Logic)
**Location:** `com.qcommerce.domain.entities`

Contains pure business logic and entities with no external dependencies.

**Key Components:**
- `Shop` - Vendor/shopkeeper entity with **MySQL POINT** for geolocation
- `Product` - Product catalog
- `ProductVariant` - SKUs with size/weight variations
- `Inventory` - Stock management with **@Version for Optimistic Locking**
- `Order` - Order lifecycle management
- `OrderItem` - Line items in orders
- `Cart` - Shopping cart
- `CartItem` - Items in cart

**Key Features:**
- ✅ **Spatial Data Types**: `@Column(columnDefinition = "POINT")` for lat/long
- ✅ **Optimistic Locking**: `@Version` on Inventory to prevent overselling
- ✅ **Rich Domain Models**: Business logic embedded in entities

---

### 2. **Application Layer** (Use Cases/Services)
**Location:** `com.qcommerce.application.services`

Orchestrates business workflows and coordinates between domain and infrastructure.

**Key Services:**

#### `ProductSearchService`
- Hyper-local product search using **MySQL ST_Distance_Sphere**
- **@Cacheable** with Redis for catalog queries
- Spatial indexing for sub-second geolocation queries

#### `EnhancedOrderService`
- Transactional order placement with inventory reservation
- **@Retryable** for handling OptimisticLockingFailureException
- WebSocket integration for real-time order updates
- Automatic inventory rollback on order cancellation

#### `CartService`
- Cart management with **Redis caching**
- Inventory validation before checkout
- **@CacheEvict** on cart modifications

#### `ShopOnboardingService`
- Vendor registration with geolocation
- Converts address to coordinates (integration point for geocoding APIs)

#### `OrderStatusWebSocketService`
- Real-time order status broadcasts via WebSocket
- Supports STOMP over SockJS

---

### 3. **Infrastructure Layer** (External Integrations)
**Location:** `com.qcommerce.infrastructure`

Handles all external concerns: database, cache, messaging, configuration.

**Components:**

#### Persistence (`infrastructure.persistence`)
- JPA Repositories with custom spatial queries
- `@Query` with native MySQL spatial functions
- `@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)` for inventory

#### Configuration (`infrastructure.config`)
- `RedisConfig` - Redis cache and session management
- `WebSocketConfig` - STOMP/SockJS configuration
- `RetryConfig` - Enables `@Retryable` with exponential backoff

#### Exception Handling (`infrastructure.exception`)
- `GlobalExceptionHandler` - Centralized error handling
- Specific handlers for `OptimisticLockingFailureException`
- Validation error mapping

---

### 4. **Interface Layer** (API Controllers)
**Location:** `com.qcommerce.interfaces.controllers`

REST API endpoints for client consumption.

**Controllers:**

#### `ProductSearchController`
**Star Feature:** Hyper-local search within radius
```java
POST /api/v1/products/search/nearby
{
  "latitude": 28.7041,
  "longitude": 77.1025,
  "radiusKm": 2.0,
  "category": "GROCERIES"
}
```
- Uses MySQL spatial indexes
- Returns shops and products sorted by distance
- Cached results for popular locations

#### `OrderController`
- Place order with optimistic locking retry
- Cancel order with inventory restoration
- Real-time status updates via WebSocket
- Order history pagination

#### `CartController`
- Add/remove/update cart items
- Redis-cached cart retrieval
- Pre-checkout inventory validation

#### `ShopController`
- Vendor onboarding
- Shop profile management
- Geolocation-based shop search

---

## 🔥 Competitive "Futuristic" Features

### 1. **MySQL Spatial Indexes**
**Why:** Standard apps filter by city (slow). Q-Commerce needs meter-level precision.

**Implementation:**
```sql
CREATE SPATIAL INDEX idx_shop_location ON shops(location);

SELECT id, name, 
  ST_Distance_Sphere(location, ST_GeomFromText('POINT(77.1025 28.7041)', 4326)) / 1000 AS distance_km
FROM shops
WHERE ST_Distance_Sphere(location, ST_GeomFromText('POINT(77.1025 28.7041)', 4326)) <= 2000
ORDER BY distance_km;
```

### 2. **Optimistic Locking for Inventory**
**Why:** Prevents negative inventory during flash sales (1000 users buying last item).

**Implementation:**
```java
@Entity
public class Inventory {
    @Version
    private Long version;  // Auto-incremented on each update
    
    public boolean reserveQuantity(Integer quantity) {
        if (availableQuantity >= quantity) {
            availableQuantity -= quantity;
            reservedQuantity += quantity;
            return true;
        }
        return false;
    }
}
```

**Retry Logic:**
```java
@Retryable(
    retryFor = {ObjectOptimisticLockingFailureException.class},
    maxAttempts = 5,
    backoff = @Backoff(delay = 100, multiplier = 2)
)
public Order placeOrder(...) { ... }
```

### 3. **Redis Caching**
**Why:** Browsing catalogs must be instant (< 100ms response time).

**Implementation:**
```java
@Cacheable(value = "productsByShop", key = "#shopId + ':' + #category")
public List<Product> getProductsByShopAndCategory(Long shopId, String category) { ... }

@CacheEvict(value = "userCart", key = "#userId")
public Cart addItemToCart(Long userId, Long variantId, Integer quantity) { ... }
```

**Cached Data:**
- Product catalogs by shop/category
- User carts
- Frequently searched locations

### 4. **WebSocket for Real-Time Updates**
**Why:** Users expect live order tracking (Preparing → Out for Delivery).

**Implementation:**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}

// Service
public void sendOrderStatusUpdate(Long orderId, String status, String message) {
    messagingTemplate.convertAndSend(
        "/topic/order/" + orderId,
        new OrderStatusUpdate(orderId, status, message, LocalDateTime.now())
    );
}
```

**Client Connection:**
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);
stompClient.connect({}, () => {
    stompClient.subscribe('/topic/order/12345', (message) => {
        console.log('Order update:', JSON.parse(message.body));
    });
});
```

---

## 🗄️ Database Schema Highlights

### Spatial Data
```sql
CREATE TABLE shops (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    location POINT NOT NULL SRID 4326,  -- MySQL 8.0 spatial type
    SPATIAL INDEX idx_shop_location (location)
);
```

### Optimistic Locking
```sql
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    available_quantity INT NOT NULL,
    reserved_quantity INT NOT NULL,
    version BIGINT NOT NULL,  -- Optimistic lock version
    INDEX idx_inventory_shop_variant (shop_id, variant_id)
);
```

### Indexes for Performance
- Spatial indexes on `shops.location`
- Composite indexes on `(shop_id, variant_id)` for inventory lookups
- Indexes on `status`, `created_at` for order queries

---

## 🚀 Performance Optimizations

1. **Database Query Optimization**
   - Spatial indexes reduce geolocation query time from O(n) to O(log n)
   - Composite indexes for multi-column queries
   - `LIMIT` clauses on all paginated queries

2. **Caching Strategy**
   - Redis for hot data (product catalogs, user sessions)
   - TTL-based cache expiration
   - Cache warming on application startup

3. **Concurrency Handling**
   - Optimistic locking with automatic retry (max 5 attempts, exponential backoff)
   - Read-heavy operations use `@Transactional(readOnly = true)`
   - Connection pooling (HikariCP)

4. **Real-Time Communication**
   - WebSocket for bidirectional communication
   - Topic-based subscriptions reduce message overhead
   - JSON message serialization

---

## 🔧 Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Language** | Java 21 (LTS) | Latest LTS with virtual threads support |
| **Framework** | Spring Boot 3.2+ | Modern enterprise framework |
| **Database** | MySQL 8.0 | Spatial data support, ACID compliance |
| **Cache** | Redis | In-memory cache for hot data |
| **ORM** | Hibernate 6.4 + Hibernate Spatial | Spatial data type support |
| **Messaging** | WebSocket (STOMP) | Real-time order updates |
| **Validation** | Jakarta Validation | Input validation |
| **Documentation** | SpringDoc OpenAPI 3 | Auto-generated API docs |
| **Testing** | Testcontainers | Integration testing with real MySQL |

---

## 📊 Data Flow Example: Order Placement

```
1. User clicks "Place Order"
   ↓
2. OrderController receives request
   ↓
3. EnhancedOrderService.placeOrder()
   ├─ Fetch Cart from Redis (CartService)
   ├─ Validate Inventory with FOR UPDATE lock
   ├─ Reserve Stock (triggers @Version check)
   │  └─ If conflict → OptimisticLockingFailureException
   │     └─ @Retryable retries up to 5 times
   ├─ Create Order entity
   ├─ Deactivate Cart (@CacheEvict)
   └─ Send WebSocket notification
   ↓
4. Response returned to client
   ↓
5. WebSocket pushes real-time update to user's browser
```

---

## 🎯 Why This Architecture Beats Competitors

| Feature | Standard E-Commerce | **This Q-Commerce Platform** |
|---------|-------------------|--------------------------|
| Location Search | Filter by city (slow) | **MySQL Spatial Indexes (< 50ms)** |
| Inventory Management | Database locks (slow) | **Optimistic Locking + Retry** |
| Catalog Speed | Every request hits DB | **Redis Caching (< 10ms)** |
| Order Updates | Poll API every 10s | **WebSocket Real-Time Push** |
| Concurrency | Pessimistic locks (serialized) | **Optimistic locks (parallelized)** |
| Scalability | Monolithic bottlenecks | **Hexagonal architecture (swappable adapters)** |

---

## 🔐 Security Considerations

1. **Input Validation**: Jakarta Validation on all DTOs
2. **SQL Injection Prevention**: JPA Criteria API and parameterized queries
3. **Optimistic Locking**: Prevents lost updates in concurrent scenarios
4. **User Authorization**: Header-based user ID (should integrate with OAuth2/JWT in production)

---

## 📈 Scalability Roadmap

### Horizontal Scaling
- **Stateless Services**: All session data in Redis
- **Load Balancer**: Nginx/HAProxy for distributing traffic
- **Database Replication**: Master-slave for read scalability

### Future Enhancements
- **Event Sourcing**: Track order lifecycle as events
- **CQRS**: Separate read/write models for orders
- **Message Queue**: Kafka/RabbitMQ for async processing
- **Microservices**: Split into Order Service, Inventory Service, Catalog Service

---

## 📚 Further Reading

- [Hexagonal Architecture Guide](https://alistair.cockburn.us/hexagonal-architecture/)
- [MySQL Spatial Data Types](https://dev.mysql.com/doc/refman/8.0/en/spatial-types.html)
- [Spring Retry Documentation](https://docs.spring.io/spring-retry/docs/current/reference/html/)
- [WebSocket with Spring Boot](https://spring.io/guides/gs/messaging-stomp-websocket/)
