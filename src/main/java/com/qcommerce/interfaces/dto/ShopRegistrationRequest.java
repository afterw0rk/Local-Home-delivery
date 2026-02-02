package com.qcommerce.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Shop Registration Request DTO
 * Used for vendor onboarding
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopRegistrationRequest {

    @NotBlank(message = "Shop name is required")
    @Size(min = 3, max = 255, message = "Shop name must be between 3 and 255 characters")
    private String shopName;

    @NotBlank(message = "Owner name is required")
    @Size(min = 3, max = 255, message = "Owner name must be between 3 and 255 characters")
    private String ownerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    // Location (required for geospatial search)
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    // Address
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 500, message = "Address line 1 too long")
    private String addressLine1;

    @Size(max = 500, message = "Address line 2 too long")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City name too long")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State name too long")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Invalid pincode format (must be 6 digits)")
    private String pincode;

    // Business Details
    @Size(max = 50, message = "GST number too long")
    private String gstNumber;

    @Size(max = 50, message = "FSSAI license too long")
    private String fssaiLicense;

    // Operational Details
    private LocalTime openingTime;

    private LocalTime closingTime;

    @DecimalMin(value = "0.1", message = "Delivery radius must be at least 0.1 km")
    @DecimalMax(value = "10.0", message = "Delivery radius cannot exceed 10 km")
    @Builder.Default
    private BigDecimal deliveryRadiusKm = BigDecimal.valueOf(2.0);

    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;
}
