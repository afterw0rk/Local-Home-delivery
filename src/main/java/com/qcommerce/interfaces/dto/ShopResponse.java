package com.qcommerce.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * Shop Response DTO
 * Returned to clients after shop operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopResponse {

    private Long id;
    private String shopName;
    private String ownerName;
    private String email;
    private String phoneNumber;

    // Location
    private Double latitude;
    private Double longitude;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;

    // Business Details
    private String gstNumber;
    private String fssaiLicense;
    private String shopStatus;

    // Operational Details
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean isOpen;
    private Boolean isOperational;

    // Delivery Configuration
    private BigDecimal deliveryRadiusKm;
    private BigDecimal minOrderAmount;

    // Distance (only when searching nearby)
    private Double distanceKm;

    // Audit
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
