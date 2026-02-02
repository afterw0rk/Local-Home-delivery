package com.qcommerce.interfaces.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for nearby product search
 * Contains product details + shop information + distance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyProductResponse {

    private Long productId;
    private String productName;
    private String description;
    private String brand;
    private String primaryImageUrl;
    private BigDecimal basePrice;
    private Boolean hasVariants;
    
    /**
     * Product variants (if available)
     */
    private List<VariantInfo> variants;
    
    /**
     * Shop information
     */
    private ShopInfo shop;
    
    /**
     * Distance from user location (in kilometers)
     */
    private Double distanceKm;
    
    /**
     * Stock availability indicator
     */
    private Boolean inStock;
    
    @Data
    @Builder
    public static class VariantInfo {
        private Long variantId;
        private String variantName;
        private String sku;
        private BigDecimal price;
        private BigDecimal mrp;
        private Integer availableQuantity;
        private String weightDisplay; // e.g., "500g", "1L"
    }
    
    @Data
    @Builder
    public static class ShopInfo {
        private Long shopId;
        private String shopName;
        private String address;
        private Double distanceKm;
        private BigDecimal minOrderAmount;
        private Boolean isOpen;
        private String estimatedDeliveryTime; // e.g., "15-20 mins"
    }
}
