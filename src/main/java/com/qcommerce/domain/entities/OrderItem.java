package com.qcommerce.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * OrderItem Entity - Line items in an order
 * Links to ProductVariant for specific SKU details
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "order_id"),
    @Index(name = "idx_order_item_variant_id", columnList = "variant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "line_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    // Snapshot of product details at order time (for historical accuracy)
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "variant_description", length = 100)
    private String variantDescription;

    /**
     * Get line total (calculate if not set)
     */
    public BigDecimal getLineTotal() {
        if (this.lineTotal == null) {
            calculateLineTotal();
        }
        return this.lineTotal;
    }

    /**
     * Calculate line total from quantity and unit price
     */
    public void calculateLineTotal() {
        this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    /**
     * Pre-persist hook to calculate totals
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        calculateLineTotal();
    }
}
