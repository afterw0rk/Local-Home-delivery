package com.qcommerce.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Entity - Represents a customer order
 * Tracks order lifecycle from PLACED to DELIVERED
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "user_id"),
    @Index(name = "idx_order_shop_id", columnList = "shop_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "delivery_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal deliveryFee;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "delivery_latitude", nullable = false)
    private Double deliveryLatitude;

    @Column(name = "delivery_longitude", nullable = false)
    private Double deliveryLongitude;

    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    @Column(name = "customer_notes", length = 1000)
    private String customerNotes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Order Status Enum
     */
    public enum OrderStatus {
        PLACED,           // Order placed by customer
        CONFIRMED,        // Shop confirmed the order
        PREPARING,        // Shop is preparing items
        READY_FOR_PICKUP, // Order ready for delivery partner
        OUT_FOR_DELIVERY, // Delivery partner picked up
        DELIVERED,        // Successfully delivered
        CANCELLED         // Order cancelled
    }

    /**
     * Helper method to add items to order
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Helper method to remove items from order
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    /**
     * Calculate total from items
     */
    public void calculateTotals() {
        this.subtotal = items.stream()
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalAmount = this.subtotal.add(this.deliveryFee);
    }

    /**
     * Check if order can be modified
     */
    public boolean isModifiable() {
        return status == OrderStatus.PLACED;
    }

    /**
     * Check if order can be cancelled
     */
    public boolean isCancellable() {
        return status == OrderStatus.PLACED || 
               status == OrderStatus.CONFIRMED || 
               status == OrderStatus.PREPARING;
    }

    /**
     * Progress order to next status
     */
    public void progressStatus() {
        switch (status) {
            case PLACED -> status = OrderStatus.CONFIRMED;
            case CONFIRMED -> status = OrderStatus.PREPARING;
            case PREPARING -> status = OrderStatus.READY_FOR_PICKUP;
            case READY_FOR_PICKUP -> status = OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> {
                status = OrderStatus.DELIVERED;
                actualDeliveryTime = LocalDateTime.now();
            }
            default -> throw new IllegalStateException("Cannot progress from status: " + status);
        }
    }
}
