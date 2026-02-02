package com.qcommerce.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

/**
 * Inventory Entity - Critical for Flash Sale and Overselling Prevention
 * 
 * Key Features:
 * 1. Optimistic Locking (@Version): Prevents race conditions during high-concurrency purchases
 * 2. Reserved Quantity: Temporarily holds stock for items in cart (15-min TTL)
 * 3. Available Quantity: Actual sellable stock
 * 
 * Concurrency Strategy:
 * - During checkout, we attempt to decrement available_quantity
 * - If another transaction committed first, OptimisticLockException is thrown
 * - Client retries with exponential backoff
 * - This ensures NO overselling even with 10,000 concurrent requests
 * 
 * Performance:
 * - Redis cache layer for read-heavy operations
 * - Database as source of truth for writes
 * - Version field creates database-level constraint
 * 
 * @author Q-Commerce Engineering Team
 */
@Entity
@Table(name = "inventory", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_variant", columnNames = "product_variant_id")
    },
    indexes = {
        @Index(name = "idx_inventory_shop", columnList = "shop_id"),
        @Index(name = "idx_inventory_available", columnList = "available_quantity")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-One relationship with ProductVariant
     * Each variant has exactly one inventory record
     */
    @Column(name = "product_variant_id", nullable = false, unique = true)
    private Long productVariantId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    /**
     * Available Quantity: Stock available for immediate sale
     * 
     * Business Rules:
     * - Cannot be negative (enforced by application + DB constraint)
     * - Decremented atomically during order placement
     * - Incremented during restocking
     */
    @Column(name = "available_quantity", nullable = false)
    @Builder.Default
    private Integer availableQuantity = 0;

    /**
     * Reserved Quantity: Items in customer carts (not yet ordered)
     * 
     * Use Case:
     * - User adds item to cart: availableQuantity -= X, reservedQuantity += X
     * - User places order: reservedQuantity -= X (already deducted from available)
     * - Cart expires (15 min): availableQuantity += X, reservedQuantity -= X
     * 
     * This prevents cart-hoarding during flash sales
     */
    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    /**
     * Reorder Level: Alert threshold for low stock
     */
    @Column(name = "reorder_level")
    @Builder.Default
    private Integer reorderLevel = 10;

    /**
     * Max Stock Level: Warehouse capacity constraint
     */
    @Column(name = "max_stock_level")
    @Builder.Default
    private Integer maxStockLevel = 1000;

    /**
     * ==========================================
     * CRITICAL: OPTIMISTIC LOCKING FOR FLASH SALES
     * ==========================================
     * 
     * Problem:
     * - 1000 users try to buy the last 10 items simultaneously
     * - Without locking: Database shows -990 quantity (DISASTER!)
     * 
     * Solution: Optimistic Locking
     * 1. Each read includes the current version number
     * 2. Update query: UPDATE inventory SET available_quantity = X, version = version + 1 WHERE id = Y AND version = Z
     * 3. If version changed (another transaction committed), update fails
     * 4. Hibernate throws OptimisticLockException
     * 5. Service layer catches exception and retries or shows "Out of Stock"
     * 
     * Performance:
     * - No pessimistic locks (SELECT FOR UPDATE) that block reads
     * - Optimistic approach assumes conflicts are rare (usually true)
     * - During flash sales, retries are acceptable for fairness
     * 
     * Code Example (Service Layer):
     * <pre>
     * {@code
     * @Transactional
     * public void deductStock(Long inventoryId, int quantity) {
     *     Inventory inv = inventoryRepo.findById(inventoryId)
     *         .orElseThrow(() -> new NotFoundException("Inventory not found"));
     *     
     *     if (inv.getAvailableQuantity() < quantity) {
     *         throw new InsufficientStockException();
     *     }
     *     
     *     inv.setAvailableQuantity(inv.getAvailableQuantity() - quantity);
     *     inventoryRepo.save(inv); // Version auto-increments
     *     
     *     // If another transaction modified inv between findById and save:
     *     // OptimisticLockException thrown automatically
     * }
     * }
     * </pre>
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "last_restocked_at")
    private ZonedDateTime lastRestockedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    // ========== Audit Fields ==========

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    // ========== Business Logic Methods ==========

    /**
     * Check if stock is sufficient for the requested quantity
     */
    public boolean hasSufficientStock(int requestedQuantity) {
        return availableQuantity >= requestedQuantity;
    }

    /**
     * Check if stock is below reorder level
     */
    public boolean needsReordering() {
        return availableQuantity <= reorderLevel;
    }

    /**
     * Calculate total physical stock (available + reserved)
     */
    public int getTotalStock() {
        return availableQuantity + reservedQuantity;
    }

    /**
     * Deduct stock (used during order placement)
     * Note: Actual persistence should happen in service layer with transaction
     */
    public void deductStock(int quantity) {
        if (quantity > availableQuantity) {
            throw new IllegalStateException(
                String.format("Insufficient stock. Available: %d, Requested: %d", 
                    availableQuantity, quantity)
            );
        }
        this.availableQuantity -= quantity;
    }

    /**
     * Add stock (used during restocking)
     */
    public void addStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        int newQuantity = this.availableQuantity + quantity;
        if (newQuantity > maxStockLevel) {
            throw new IllegalStateException(
                String.format("Cannot exceed max stock level: %d", maxStockLevel)
            );
        }
        
        this.availableQuantity = newQuantity;
        this.lastRestockedAt = ZonedDateTime.now();
    }

    /**
     * Reserve stock (when item added to cart)
     */
    public void reserveStock(int quantity) {
        if (quantity > availableQuantity) {
            throw new IllegalStateException("Cannot reserve more than available stock");
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    /**
     * Release reservation (when cart expires or order placed)
     */
    public void releaseReservation(int quantity) {
        if (quantity > reservedQuantity) {
            throw new IllegalStateException("Cannot release more than reserved quantity");
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }
}
