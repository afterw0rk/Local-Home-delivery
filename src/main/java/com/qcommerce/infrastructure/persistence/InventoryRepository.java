package com.qcommerce.infrastructure.persistence;

import com.qcommerce.domain.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Inventory Repository - Concurrency-safe stock management
 * 
 * Key Features:
 * - Optimistic locking for flash sale scenarios
 * - Atomic stock deduction queries
 * - Low stock alerts
 * 
 * @author Q-Commerce Engineering Team
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory by product variant ID
     */
    Optional<Inventory> findByProductVariantId(Long productVariantId);
    
    /**
     * Find inventory by multiple variant IDs (batch operation)
     */
    List<Inventory> findByProductVariantIdIn(List<Long> productVariantIds);

    /**
     * Find inventory with optimistic lock
     * Use this when you need to ensure no concurrent modifications
     */
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT i FROM Inventory i WHERE i.productVariantId = :variantId")
    Optional<Inventory> findByProductVariantIdWithLock(@Param("variantId") Long variantId);

    /**
     * CRITICAL: Atomic stock deduction using native query
     * 
     * This is a safer alternative to optimistic locking for high-concurrency scenarios
     * Updates only if sufficient stock exists
     * 
     * Returns number of rows affected:
     * - 1 = Success (stock deducted)
     * - 0 = Failure (insufficient stock or concurrent update)
     * 
     * Example:
     * int updated = inventoryRepository.deductStockAtomic(variantId, 5, currentVersion);
     * if (updated == 0) {
     *     throw new InsufficientStockException("Out of stock or concurrent update");
     * }
     */
    @Modifying
    @Query(value = """
        UPDATE inventory 
        SET available_quantity = available_quantity - :quantity,
            version = version + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE product_variant_id = :variantId
          AND available_quantity >= :quantity
          AND version = :expectedVersion
        """, nativeQuery = true)
    int deductStockAtomic(
        @Param("variantId") Long variantId,
        @Param("quantity") Integer quantity,
        @Param("expectedVersion") Integer expectedVersion
    );

    /**
     * Reserve stock for cart (reduces available, increases reserved)
     */
    @Modifying
    @Query(value = """
        UPDATE inventory 
        SET available_quantity = available_quantity - :quantity,
            reserved_quantity = reserved_quantity + :quantity,
            version = version + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE product_variant_id = :variantId
          AND available_quantity >= :quantity
        """, nativeQuery = true)
    int reserveStockAtomic(
        @Param("variantId") Long variantId,
        @Param("quantity") Integer quantity
    );

    /**
     * Release reserved stock (when cart expires)
     */
    @Modifying
    @Query(value = """
        UPDATE inventory 
        SET available_quantity = available_quantity + :quantity,
            reserved_quantity = reserved_quantity - :quantity,
            version = version + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE product_variant_id = :variantId
          AND reserved_quantity >= :quantity
        """, nativeQuery = true)
    int releaseReservedStockAtomic(
        @Param("variantId") Long variantId,
        @Param("quantity") Integer quantity
    );

    /**
     * Add stock (restocking operation)
     */
    @Modifying
    @Query(value = """
        UPDATE inventory 
        SET available_quantity = available_quantity + :quantity,
            last_restocked_at = CURRENT_TIMESTAMP,
            version = version + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE product_variant_id = :variantId
        """, nativeQuery = true)
    int addStockAtomic(
        @Param("variantId") Long variantId,
        @Param("quantity") Integer quantity
    );

    /**
     * Find all inventory records for a shop
     */
    @Query("SELECT i FROM Inventory i WHERE i.shopId = :shopId")
    List<Inventory> findByShopId(@Param("shopId") Long shopId);

    /**
     * Find low stock items (below reorder level)
     */
    @Query("SELECT i FROM Inventory i WHERE i.shopId = :shopId AND i.availableQuantity <= i.reorderLevel")
    List<Inventory> findLowStockItems(@Param("shopId") Long shopId);

    /**
     * Find out-of-stock items
     */
    @Query("SELECT i FROM Inventory i WHERE i.shopId = :shopId AND i.availableQuantity = 0")
    List<Inventory> findOutOfStockItems(@Param("shopId") Long shopId);

    /**
     * Check if variant has sufficient stock
     */
    @Query("SELECT CASE WHEN i.availableQuantity >= :quantity THEN true ELSE false END " +
           "FROM Inventory i WHERE i.productVariantId = :variantId")
    boolean hasSufficientStock(
        @Param("variantId") Long variantId,
        @Param("quantity") Integer quantity
    );
}
