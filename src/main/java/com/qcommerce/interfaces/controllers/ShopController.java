package com.qcommerce.interfaces.controllers;

import com.qcommerce.application.services.ShopOnboardingService;
import com.qcommerce.interfaces.dto.ShopRegistrationRequest;
import com.qcommerce.interfaces.dto.ShopResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Shop Controller - Vendor Onboarding & Management
 * 
 * Endpoints:
 * 1. POST /api/v1/shops/register - Shopkeeper registration
 * 2. GET /api/v1/shops/{id} - Get shop details
 * 3. GET /api/v1/shops/nearby - Find nearby shops
 * 4. PUT /api/v1/shops/{id}/status - Approve/reject shop
 * 
 * @author Q-Commerce Engineering Team
 */
@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shop Management", description = "Vendor onboarding and shop management APIs")
public class ShopController {

    private final ShopOnboardingService shopOnboardingService;

    /**
     * Register new shop (Vendor onboarding)
     * 
     * Example Request:
     * POST /api/v1/shops/register
     * {
     *   "shopName": "Quick Mart Express",
     *   "ownerName": "Rajesh Kumar",
     *   "email": "rajesh@quickmart.com",
     *   "phoneNumber": "+919876543210",
     *   "latitude": 12.9716,
     *   "longitude": 77.5946,
     *   "addressLine1": "123, MG Road",
     *   "city": "Bangalore",
     *   "state": "Karnataka",
     *   "pincode": "560001",
     *   "gstNumber": "29ABCDE1234F1Z5",
     *   "deliveryRadiusKm": 3.0
     * }
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new shop",
        description = "Onboard a new vendor/shopkeeper into the platform. " +
                      "Shop will be in PENDING status until admin approval."
    )
    public ResponseEntity<ShopResponse> registerShop(
        @Valid @RequestBody ShopRegistrationRequest request
    ) {
        log.info("New shop registration request - Email: {}, City: {}", 
            request.getEmail(), request.getCity());

        ShopResponse response = shopOnboardingService.registerShop(request);

        log.info("✅ Shop registered successfully - ID: {}, Status: {}", 
            response.getId(), response.getShopStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get shop details by ID
     */
    @GetMapping("/{shopId}")
    @Operation(summary = "Get shop details", description = "Retrieve shop information by ID")
    public ResponseEntity<ShopResponse> getShopById(@PathVariable Long shopId) {
        ShopResponse shop = shopOnboardingService.getShopById(shopId);
        return ResponseEntity.ok(shop);
    }

    /**
     * Find nearby shops (for admin/analytics)
     */
    @GetMapping("/nearby")
    @Operation(
        summary = "Find nearby shops",
        description = "Get all shops within specified radius of a location"
    )
    public ResponseEntity<Page<ShopResponse>> getNearbyShops(
        @RequestParam Double latitude,
        @RequestParam Double longitude,
        @RequestParam(defaultValue = "5.0") Double radiusKm,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        Page<ShopResponse> shops = shopOnboardingService.findNearbyShops(
            latitude, longitude, radiusKm, PageRequest.of(page, pageSize)
        );

        return ResponseEntity.ok(shops);
    }

    /**
     * Update shop status (Admin only)
     * 
     * Example: Approve a shop
     * PUT /api/v1/shops/123/status?status=APPROVED
     */
    @PutMapping("/{shopId}/status")
    @Operation(
        summary = "Update shop status",
        description = "Approve, suspend, or reject a shop (Admin only)"
    )
    public ResponseEntity<ShopResponse> updateShopStatus(
        @PathVariable Long shopId,
        @RequestParam String status
    ) {
        log.info("Updating shop status - ID: {}, New Status: {}", shopId, status);

        ShopResponse shop = shopOnboardingService.updateShopStatus(shopId, status);

        log.info("✅ Shop status updated - ID: {}, Status: {}", shopId, shop.getShopStatus());

        return ResponseEntity.ok(shop);
    }

    /**
     * Update shop details
     */
    @PutMapping("/{shopId}")
    @Operation(summary = "Update shop details", description = "Update shop information")
    public ResponseEntity<ShopResponse> updateShop(
        @PathVariable Long shopId,
        @Valid @RequestBody ShopRegistrationRequest request
    ) {
        ShopResponse shop = shopOnboardingService.updateShop(shopId, request);
        return ResponseEntity.ok(shop);
    }

    /**
     * Get all shops with filters
     */
    @GetMapping
    @Operation(summary = "List shops", description = "Get all shops with pagination and filters")
    public ResponseEntity<Page<ShopResponse>> getAllShops(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String city,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        Page<ShopResponse> shops = shopOnboardingService.getAllShops(
            status, city, PageRequest.of(page, pageSize)
        );

        return ResponseEntity.ok(shops);
    }
}
