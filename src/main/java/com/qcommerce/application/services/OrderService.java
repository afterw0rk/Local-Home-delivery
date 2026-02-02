package com.qcommerce.application.services;

import com.qcommerce.domain.entities.Inventory;
import com.qcommerce.infrastructure.persistence.InventoryRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order Service - Handles order placement with inventory management
 * 
 * Key Features:
 * 1. Optimistic Locking with automatic retry
 * 2. Atomic stock deduction
 * 3. Real-time order status updates via WebSocket
 * 
 * Concurrency Strategy:
 * - Attempt stock deduction
 * - If OptimisticLockException thrown (someone else modified inventory):
 *   - Retry up to 3 times with exponential backoff (100ms, 200ms, 400ms)
 *   - If all retries fail: Show "Out of Stock" to user
 * 
 * Why Optimistic Locking over Pessimistic?
 * - No database locks (better performance)
 * - Conflicts are rare in normal operation
 * - During flash sales, fair first-come-first-served
 * 
 * @author Q-Commerce Engineering Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final InventoryRepository inventoryRepository;
    private final OrderStatusWebSocketService webSocketService;

    /**
     * ==========================================
     * CRITICAL: Place Order with Stock Management
     * ==========================================
     * 
     * Flow:
     * 1. Validate inventory availability
     * 2. Deduct stock (with optimistic lock protection)
     * 3. Create order record
     * 4. Broadcast real-time update via WebSocket
     * 
     * Scenario: Flash Sale (1000 users, 10 items left)
     * 
     * Without Locking:
     * - All 1000 reads see "10 available"
     * - All 1000 try to buy
     * - Result: -990 inventory (DISASTER!)
     * 
     * With Optimistic Locking:
     * - First 10 users: SUCCESS (version increments)
     * - Next 990 users: OptimisticLockException
     * - Retry mechanism: Check again
     * - Eventually: "Out of Stock" shown
     * 
     * @param variantId Product variant to purchase
     * @param quantity Requested quantity
     * @return Order ID if successful
     * @throws InsufficientStockException if not enough stock
     * @throws OptimisticLockException if max retries exceeded
     */
    @Transactional
    @Retryable(
        retryFor = {OptimisticLockException.class, ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Long placeOrder(Long variantId, Integer quantity, Long userId) {
        log.info("Attempting to place order - Variant: {}, Quantity: {}, User: {}", 
            variantId, quantity, userId);

        // Step 1: Fetch inventory with current version
        Inventory inventory = inventoryRepository.findByProductVariantId(variantId)
            .orElseThrow(() -> new IllegalArgumentException("Product variant not found"));

        log.debug("Current inventory - Available: {}, Reserved: {}, Version: {}",
            inventory.getAvailableQuantity(), 
            inventory.getReservedQuantity(),
            inventory.getVersion());

        // Step 2: Validate stock availability
        if (!inventory.hasSufficientStock(quantity)) {
            log.warn("Insufficient stock - Requested: {}, Available: {}", 
                quantity, inventory.getAvailableQuantity());
            throw new InsufficientStockException(
                String.format("Only %d items available", inventory.getAvailableQuantity())
            );
        }

        // Step 3: Deduct stock
        // CRITICAL: The save() below will fail if version changed
        inventory.deductStock(quantity);
        
        try {
            // This update includes: SET version = version + 1 WHERE id = ? AND version = ?
            inventoryRepository.save(inventory);
            log.info("✅ Stock deducted successfully - New quantity: {}", 
                inventory.getAvailableQuantity());
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            log.warn("⚠️ Optimistic lock conflict - Retrying... (Attempt will auto-retry)", e);
            throw e; // @Retryable will catch and retry
        }

        // Step 4: Create order (simplified - actual implementation would be more complex)
        Long orderId = createOrderRecord(userId, variantId, quantity);

        // Step 5: Broadcast real-time update
        webSocketService.broadcastOrderUpdate(
            orderId,
            "PAYMENT_PENDING",
            "Order placed successfully! Complete payment to proceed.",
            null
        );

        // Step 6: Update inventory cache (invalidate or update)
        webSocketService.broadcastInventoryUpdate(
            inventory.getShopId(),
            variantId,
            inventory.getAvailableQuantity()
        );

        return orderId;
    }

    /**
     * Alternative: Atomic stock deduction using native query
     * 
     * This is even safer for high-concurrency scenarios
     * The entire operation happens in a single database statement
     */
    @Transactional
    public Long placeOrderAtomic(Long variantId, Integer quantity, Long userId) {
        log.info("Attempting atomic order placement - Variant: {}, Quantity: {}", 
            variantId, quantity);

        // Fetch current inventory
        Inventory inventory = inventoryRepository.findByProductVariantId(variantId)
            .orElseThrow(() -> new IllegalArgumentException("Product variant not found"));

        // Atomic deduction using native query
        int rowsAffected = inventoryRepository.deductStockAtomic(
            variantId,
            quantity,
            inventory.getVersion()
        );

        if (rowsAffected == 0) {
            log.warn("❌ Atomic stock deduction failed - Out of stock or concurrent update");
            
            // Check if it's actually out of stock or just a version conflict
            Inventory refreshed = inventoryRepository.findByProductVariantId(variantId)
                .orElseThrow();
            
            if (refreshed.getAvailableQuantity() < quantity) {
                throw new InsufficientStockException(
                    String.format("Only %d items available", refreshed.getAvailableQuantity())
                );
            } else {
                // Retry once
                return placeOrderAtomic(variantId, quantity, userId);
            }
        }

        log.info("✅ Atomic stock deduction successful");

        // Create order and broadcast updates
        Long orderId = createOrderRecord(userId, variantId, quantity);
        
        webSocketService.broadcastOrderUpdate(
            orderId,
            "PAYMENT_PENDING",
            "Order placed successfully!",
            null
        );

        return orderId;
    }

    /**
     * Reserve stock for cart (temporary hold)
     * 
     * Use Case:
     * - User adds item to cart
     * - Stock is "reserved" for 15 minutes
     * - Prevents others from buying during checkout
     * - Released if cart expires or order cancelled
     */
    @Transactional
    public void reserveStockForCart(Long variantId, Integer quantity) {
        int rowsAffected = inventoryRepository.reserveStockAtomic(variantId, quantity);
        
        if (rowsAffected == 0) {
            throw new InsufficientStockException("Cannot reserve - insufficient stock");
        }
        
        log.info("✅ Reserved {} units for variant {}", quantity, variantId);
    }

    /**
     * Release reserved stock (cart expired)
     */
    @Transactional
    public void releaseReservedStock(Long variantId, Integer quantity) {
        int rowsAffected = inventoryRepository.releaseReservedStockAtomic(variantId, quantity);
        
        if (rowsAffected > 0) {
            log.info("✅ Released {} reserved units for variant {}", quantity, variantId);
        }
    }

    /**
     * Simplified order record creation (actual implementation would be more complex)
     */
    private Long createOrderRecord(Long userId, Long variantId, Integer quantity) {
        // TODO: Create order in orders table
        // TODO: Create order_items record
        // TODO: Calculate totals, apply discounts, etc.
        
        Long orderId = System.currentTimeMillis(); // Mock order ID
        log.info("📦 Order created - ID: {}, User: {}, Variant: {}, Quantity: {}", 
            orderId, userId, variantId, quantity);
        
        return orderId;
    }

    /**
     * Custom exception for insufficient stock
     */
    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
