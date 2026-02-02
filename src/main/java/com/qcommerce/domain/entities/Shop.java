package com.qcommerce.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * Shop Entity - Represents a vendor/shopkeeper in the Q-Commerce platform
 * 
 * Key Features:
 * 1. Spatial Data Type: Uses JTS Point for MySQL POINT storage (Lat/Long)
 * 2. Optimistic Locking: @Version prevents concurrent modification conflicts
 * 3. SRID 4326: Standard GPS coordinate system (WGS 84)
 * 
 * Database Notes:
 * - SPATIAL INDEX on 'location' column for O(log n) geospatial queries
 * - ST_Distance_Sphere() used for radius-based searches
 * 
 * @author Q-Commerce Engineering Team
 */
@Entity
@Table(name = "shops", indexes = {
    @Index(name = "idx_shop_status", columnList = "shop_status"),
    @Index(name = "idx_shop_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /**
     * CRITICAL: Spatial Data Type for Geolocation
     * 
     * - Stored as MySQL POINT(longitude, latitude)
     * - SRID 4326 = WGS 84 GPS coordinate system
     * - Enables ST_Distance_Sphere() for accurate radius calculations
     * - SPATIAL INDEX required for performance (see schema)
     * 
     * Usage:
     * GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
     * Point location = gf.createPoint(new Coordinate(longitude, latitude));
     */
    @Column(name = "location", columnDefinition = "POINT SRID 4326", nullable = false)
    private Point location;

    @Column(name = "address_line1", nullable = false, length = 500)
    private String addressLine1;

    @Column(name = "address_line2", length = 500)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Column(name = "gst_number", length = 50)
    private String gstNumber;

    @Column(name = "fssai_license", length = 50)
    private String fssaiLicense;

    @Enumerated(EnumType.STRING)
    @Column(name = "shop_status", nullable = false)
    @Builder.Default
    private ShopStatus shopStatus = ShopStatus.PENDING;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "is_open")
    @Builder.Default
    private Boolean isOpen = true;

    /**
     * Delivery Configuration
     * Defines the maximum distance (in km) the shop delivers to
     */
    @Column(name = "delivery_radius_km", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal deliveryRadiusKm = BigDecimal.valueOf(2.00);

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    // ========== Audit Fields ==========

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * CRITICAL: Optimistic Locking Version
     * 
     * - Prevents lost updates in concurrent scenarios
     * - Automatically incremented by JPA on each update
     * - Throws OptimisticLockException if version mismatch
     * 
     * Use Case: Multiple admins updating shop details simultaneously
     */
    @Version
    @Column(name = "version")
    private Integer version;

    // ========== Enums ==========

    public enum ShopStatus {
        PENDING,    // Awaiting approval
        APPROVED,   // Active and operational
        SUSPENDED,  // Temporarily disabled
        REJECTED    // Application rejected
    }

    // ========== Helper Methods ==========

    /**
     * Get latitude from Point geometry
     */
    public Double getLatitude() {
        return location != null ? location.getY() : null;
    }

    /**
     * Get longitude from Point geometry
     */
    public Double getLongitude() {
        return location != null ? location.getX() : null;
    }

    /**
     * Business logic: Check if shop is currently operational
     */
    public boolean isOperational() {
        if (!isOpen || shopStatus != ShopStatus.APPROVED) {
            return false;
        }
        
        if (openingTime == null || closingTime == null) {
            return true; // 24/7 operation
        }
        
        LocalTime now = LocalTime.now();
        return !now.isBefore(openingTime) && !now.isAfter(closingTime);
    }
}
