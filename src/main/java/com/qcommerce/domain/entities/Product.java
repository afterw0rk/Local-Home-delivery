package com.qcommerce.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Product Entity - Master product catalog
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_shop", columnList = "shop_id"),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", insertable = false, updatable = false)
    private Shop shop;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "product_name", nullable = false, length = 500)
    private String productName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "brand")
    private String brand;

    @Column(name = "primary_image_url", length = 500)
    private String primaryImageUrl;

    @Column(name = "additional_images", columnDefinition = "JSON")
    private String additionalImages;

    @Column(name = "has_variants")
    @Builder.Default
    private Boolean hasVariants = false;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "slug", unique = true, length = 500)
    private String slug;

    @Version
    @Column(name = "version")
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
