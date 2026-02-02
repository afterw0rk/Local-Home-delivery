package com.qcommerce.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order Placement Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacementRequest {

    @NotBlank(message = "Delivery address is required")
    @Size(min = 10, max = 500, message = "Delivery address must be between 10 and 500 characters")
    private String deliveryAddress;

    @NotNull(message = "Delivery latitude is required")
    @DecimalMin(value = "-90.0", message = "Delivery latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Delivery latitude must be <= 90")
    private Double deliveryLatitude;

    @NotNull(message = "Delivery longitude is required")
    @DecimalMin(value = "-180.0", message = "Delivery longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Delivery longitude must be <= 180")
    private Double deliveryLongitude;

    @Size(max = 500, message = "Customer notes cannot exceed 500 characters")
    private String customerNotes;
}
