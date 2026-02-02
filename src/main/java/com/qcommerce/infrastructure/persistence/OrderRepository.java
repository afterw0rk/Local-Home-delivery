package com.qcommerce.infrastructure.persistence;

import com.qcommerce.domain.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Repository - Data access for orders
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by user ID
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find orders by shop ID
     */
    Page<Order> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    /**
     * Find orders by status
     */
    List<Order> findByStatus(Order.OrderStatus status);

    /**
     * Find orders by user and status
     */
    Page<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status, Pageable pageable);

    /**
     * Find active orders for a shop
     */
    @Query("SELECT o FROM Order o WHERE o.shop.id = :shopId " +
           "AND o.status IN ('CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY') " +
           "ORDER BY o.createdAt ASC")
    List<Order> findActiveOrdersByShop(@Param("shopId") Long shopId);

    /**
     * Count orders by status and shop
     */
    long countByShopIdAndStatus(Long shopId, Order.OrderStatus status);

    /**
     * Find orders created within date range
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY o.createdAt DESC")
    List<Order> findOrdersInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
