package com.qcommerce.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String deliveryAddress;

    @NotNull(message = "Delivery latitude is required")
    private Double deliveryLatitude;

    @NotNull(message = "Delivery longitude is required")
    private Double deliveryLongitude;

    private String customerNotes;
}
