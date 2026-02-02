package com.qcommerce.interfaces.controllers;

import com.qcommerce.application.services.CartService;
import com.qcommerce.domain.entities.Cart;
import com.qcommerce.interfaces.dto.AddToCartRequest;
import com.qcommerce.interfaces.dto.CartResponse;
import com.qcommerce.interfaces.dto.UpdateCartItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Cart Controller - Shopping cart management
 */
@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping cart management APIs")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Get active cart for user
     */
    @GetMapping
    @Operation(summary = "Get cart", 
               description = "Retrieve user's active cart (cached in Redis)")
    public ResponseEntity<CartResponse> getCart(@RequestHeader("X-User-Id") Long userId) {
        log.info("[v0] Get cart request for user: {}", userId);

        Cart cart = cartService.getActiveCart(userId);
        
        if (cart == null) {
            return ResponseEntity.ok(CartResponse.builder()
                .items(java.util.Collections.emptyList())
                .totalItems(0)
                .build());
        }

        CartResponse response = mapToCartResponse(cart);
        return ResponseEntity.ok(response);
    }

    /**
     * Add item to cart
     */
    @PostMapping("/items")
    @Operation(summary = "Add to cart", 
               description = "Add product variant to cart with inventory validation")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("[v0] Add to cart request: userId={}, variantId={}, quantity={}", 
                 userId, request.getVariantId(), request.getQuantity());

        Cart cart = cartService.addItemToCart(
            userId,
            request.getVariantId(),
            request.getQuantity()
        );

        CartResponse response = mapToCartResponse(cart);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update cart item quantity
     */
    @PatchMapping("/items/{variantId}")
    @Operation(summary = "Update cart item", 
               description = "Update quantity of item in cart")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long variantId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("[v0] Update cart item: userId={}, variantId={}, newQuantity={}", 
                 userId, variantId, request.getQuantity());

        Cart cart = cartService.updateItemQuantity(
            userId,
            variantId,
            request.getQuantity()
        );

        CartResponse response = mapToCartResponse(cart);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/items/{variantId}")
    @Operation(summary = "Remove from cart", 
               description = "Remove product variant from cart")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long variantId,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("[v0] Remove from cart: userId={}, variantId={}", userId, variantId);

        Cart cart = cartService.removeItemFromCart(userId, variantId);
        CartResponse response = mapToCartResponse(cart);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Clear cart
     */
    @DeleteMapping
    @Operation(summary = "Clear cart", 
               description = "Remove all items from cart")
    public ResponseEntity<Void> clearCart(@RequestHeader("X-User-Id") Long userId) {
        log.info("[v0] Clear cart request for user: {}", userId);

        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validate cart inventory
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate cart", 
               description = "Check if all cart items are available in inventory")
    public ResponseEntity<Boolean> validateCart(@RequestHeader("X-User-Id") Long userId) {
        log.info("[v0] Validate cart request for user: {}", userId);

        boolean isValid = cartService.validateCartInventory(userId);
        return ResponseEntity.ok(isValid);
    }

    /**
     * Map Cart entity to response DTO
     */
    private CartResponse mapToCartResponse(Cart cart) {
        return CartResponse.builder()
            .id(cart.getId())
            .shopId(cart.getShop().getId())
            .shopName(cart.getShop().getName())
            .items(cart.getItems().stream()
                .map(item -> CartResponse.CartItemResponse.builder()
                    .variantId(item.getVariant().getId())
                    .productName(item.getVariant().getProduct().getName())
                    .variantDescription(item.getVariant().getSize() + " - " + 
                                       item.getVariant().getWeight())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getVariant().getPrice())
                    .lineTotal(item.getVariant().getPrice()
                              .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build())
                .collect(Collectors.toList()))
            .totalItems(cart.getTotalItemsCount())
            .build();
    }
}
