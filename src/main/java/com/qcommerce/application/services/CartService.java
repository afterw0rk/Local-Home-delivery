package com.qcommerce.application.services;

import com.qcommerce.domain.entities.*;
import com.qcommerce.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * CartService - Manages shopping cart operations
 * Integrates with Redis cache for fast cart retrieval
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final ShopRepository shopRepository;

    /**
     * Get or create active cart for user
     * Cached in Redis for performance
     */
    @Cacheable(value = "userCart", key = "#userId")
    @Transactional(readOnly = true)
    public Cart getActiveCart(Long userId) {
        log.info("[v0] Fetching active cart for user: {}", userId);
        
        return cartRepository.findActiveCartByUserWithItems(userId)
            .orElse(null);
    }

    /**
     * Add item to cart with inventory validation
     */
    @CacheEvict(value = "userCart", key = "#userId")
    public Cart addItemToCart(Long userId, Long variantId, Integer quantity) {
        log.info("[v0] Adding item to cart - userId: {}, variantId: {}, quantity: {}", 
                 userId, variantId, quantity);

        // Validate variant exists and is active
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new IllegalArgumentException("Product variant not found"));

        if (!variant.getIsActive()) {
            throw new IllegalStateException("Product variant is not available");
        }

        // Check inventory availability
        Inventory inventory = inventoryRepository
            .findByProductVariantIdAndShopId(variantId, variant.getProduct().getShop().getId())
            .orElseThrow(() -> new IllegalStateException("Inventory not found"));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new IllegalStateException(
                String.format("Insufficient inventory. Available: %d, Requested: %d", 
                             inventory.getAvailableQuantity(), quantity)
            );
        }

        // Get or create cart for this shop
        Cart cart = cartRepository.findByUserIdAndShopIdAndIsActiveTrue(
            userId, variant.getProduct().getShop().getId()
        ).orElseGet(() -> createNewCart(userId, variant.getProduct().getShop().getId()));

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getVariant().getId().equals(variantId))
            .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            // Validate total quantity against inventory
            if (inventory.getAvailableQuantity() < newQuantity) {
                throw new IllegalStateException("Insufficient inventory for requested quantity");
            }
            
            item.setQuantity(newQuantity);
            log.info("[v0] Updated cart item quantity to: {}", newQuantity);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                .cart(cart)
                .variant(variant)
                .quantity(quantity)
                .build();
            
            cart.addItem(newItem);
            log.info("[v0] Added new item to cart");
        }

        return cartRepository.save(cart);
    }

    /**
     * Update item quantity in cart
     */
    @CacheEvict(value = "userCart", key = "#userId")
    public Cart updateItemQuantity(Long userId, Long variantId, Integer newQuantity) {
        log.info("[v0] Updating cart item - userId: {}, variantId: {}, newQuantity: {}", 
                 userId, variantId, newQuantity);

        Cart cart = cartRepository.findActiveCartByUserWithItems(userId)
            .orElseThrow(() -> new IllegalStateException("Active cart not found"));

        if (newQuantity <= 0) {
            return removeItemFromCart(userId, variantId);
        }

        CartItem item = cart.getItems().stream()
            .filter(ci -> ci.getVariant().getId().equals(variantId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));

        // Validate inventory
        Inventory inventory = inventoryRepository
            .findByProductVariantIdAndShopId(variantId, cart.getShop().getId())
            .orElseThrow(() -> new IllegalStateException("Inventory not found"));

        if (inventory.getAvailableQuantity() < newQuantity) {
            throw new IllegalStateException("Insufficient inventory");
        }

        item.setQuantity(newQuantity);
        return cartRepository.save(cart);
    }

    /**
     * Remove item from cart
     */
    @CacheEvict(value = "userCart", key = "#userId")
    public Cart removeItemFromCart(Long userId, Long variantId) {
        log.info("[v0] Removing item from cart - userId: {}, variantId: {}", userId, variantId);

        Cart cart = cartRepository.findActiveCartByUserWithItems(userId)
            .orElseThrow(() -> new IllegalStateException("Active cart not found"));

        CartItem itemToRemove = cart.getItems().stream()
            .filter(item -> item.getVariant().getId().equals(variantId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));

        cart.removeItem(itemToRemove);
        return cartRepository.save(cart);
    }

    /**
     * Clear all items from cart
     */
    @CacheEvict(value = "userCart", key = "#userId")
    public void clearCart(Long userId) {
        log.info("[v0] Clearing cart for user: {}", userId);

        cartRepository.findActiveCartByUserWithItems(userId)
            .ifPresent(cart -> {
                cart.clearItems();
                cartRepository.save(cart);
            });
    }

    /**
     * Deactivate cart (after order placement)
     */
    @CacheEvict(value = "userCart", key = "#userId")
    public void deactivateCart(Long userId) {
        log.info("[v0] Deactivating cart for user: {}", userId);

        cartRepository.findByUserIdAndIsActiveTrue(userId)
            .ifPresent(cart -> {
                cart.setIsActive(false);
                cartRepository.save(cart);
            });
    }

    /**
     * Create new cart for user and shop
     */
    private Cart createNewCart(Long userId, Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        Cart newCart = Cart.builder()
            .userId(userId)
            .shop(shop)
            .isActive(true)
            .build();

        return cartRepository.save(newCart);
    }

    /**
     * Validate cart items against current inventory
     * Returns true if all items are available
     */
    @Transactional(readOnly = true)
    public boolean validateCartInventory(Long userId) {
        Cart cart = getActiveCart(userId);
        
        if (cart == null || cart.getItems().isEmpty()) {
            return false;
        }

        return cart.getItems().stream().allMatch(item -> {
            Optional<Inventory> inventory = inventoryRepository
                .findByProductVariantIdAndShopId(
                    item.getVariant().getId(), 
                    cart.getShop().getId()
                );
            
            return inventory.isPresent() && 
                   inventory.get().getAvailableQuantity() >= item.getQuantity();
        });
    }
}
