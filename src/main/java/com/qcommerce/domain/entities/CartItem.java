package com.qcommerce.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * CartItem Entity - Individual items in a cart
 */
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_item_cart_id", columnList = "cart_id"),
    @Index(name = "idx_cart_item_variant_id", columnList = "variant_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_cart_variant", columnNames = {"cart_id", "variant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @UpdateTimestamp
    @Column(name = "added_at")
    private LocalDateTime addedAt;

    /**
     * Increase quantity
     */
    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    /**
     * Decrease quantity
     */
    public void decreaseQuantity(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
    }
}
