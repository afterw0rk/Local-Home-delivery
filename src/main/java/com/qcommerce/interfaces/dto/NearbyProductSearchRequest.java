package com.qcommerce.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for nearby product search
 * Hyper-local discovery based on user geolocation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyProductSearchRequest {

    /**
     * User's current latitude
     * Valid range: -90 to +90
     */
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    /**
     * User's current longitude
     * Valid range: -180 to +180
     */
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    /**
     * Search radius in kilometers
     * Default: 2km (typical Q-Commerce delivery range)
     */
    @DecimalMin(value = "0.1", message = "Radius must be at least 0.1 km")
    @DecimalMax(value = "10.0", message = "Radius cannot exceed 10 km")
    @Builder.Default
    private Double radiusKm = 2.0;

    /**
     * Optional: Category filter
     */
    private Long categoryId;

    /**
     * Optional: Search keyword for product name/description
     */
    @Size(max = 255, message = "Search query too long")
    private String searchKeyword;

    /**
     * Pagination
     */
    @Min(value = 0, message = "Page number must be >= 0")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be >= 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Builder.Default
    private Integer pageSize = 20;

    /**
     * Sorting options
     */
    private SortBy sortBy = SortBy.DISTANCE;

    public enum SortBy {
        DISTANCE,        // Nearest first (default for Q-Commerce)
        PRICE_LOW_HIGH,
        PRICE_HIGH_LOW,
        POPULARITY,
        RATING
    }
}
