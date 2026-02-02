package com.qcommerce.application.services;

import com.qcommerce.domain.entities.*;
import com.qcommerce.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * EnhancedOrderService - Complete order processing with optimistic locking
 * Implements retry logic for handling concurrent order placement during flash sales
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EnhancedOrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final InventoryRepository inventoryRepository;
    private final OrderStatusWebSocketService webSocketService;

    /**
     * Place order from cart with optimistic locking retry
     * Automatically retries on OptimisticLockingFailureException
     */
    @Retryable(
        retryFor = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Order placeOrder(Long userId, String deliveryAddress, 
                           Double deliveryLat, Double deliveryLng, 
                           String customerNotes) {
        log.info("[v0] Placing order for user: {}", userId);

        // Get active cart
        Cart cart = cartService.getActiveCart(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Validate inventory and reserve stock (with optimistic locking)
        for (CartItem cartItem : cart.getItems()) {
            Inventory inventory = inventoryRepository
                .findByProductVariantIdAndShopIdWithLock(
                    cartItem.getVariant().getId(),
                    cart.getShop().getId()
                )
                .orElseThrow(() -> new IllegalStateException(
                    "Inventory not found for product: " + cartItem.getVariant().getProduct().getName()
                ));

            // Check availability
            if (inventory.getAvailableQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException(
                    String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                        cartItem.getVariant().getProduct().getName(),
                        inventory.getAvailableQuantity(),
                        cartItem.getQuantity())
                );
            }

            // Reserve inventory (this will trigger optimistic lock check on commit)
            boolean reserved = inventory.reserveQuantity(cartItem.getQuantity());
            if (!reserved) {
                throw new IllegalStateException("Failed to reserve inventory");
            }

            inventoryRepository.save(inventory);
            log.info("[v0] Reserved {} units of product: {}", 
                     cartItem.getQuantity(), 
                     cartItem.getVariant().getProduct().getName());
        }

        // Create order
        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .userId(userId)
            .shop(cart.getShop())
            .status(Order.OrderStatus.PLACED)
            .deliveryAddress(deliveryAddress)
            .deliveryLatitude(deliveryLat)
            .deliveryLongitude(deliveryLng)
            .customerNotes(customerNotes)
            .deliveryFee(calculateDeliveryFee(cart.getShop(), deliveryLat, deliveryLng))
            .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(30))
            .build();

        // Add order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                .variant(cartItem.getVariant())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getVariant().getPrice())
                .productName(cartItem.getVariant().getProduct().getName())
                .variantDescription(cartItem.getVariant().getSize() + " - " + 
                                   cartItem.getVariant().getWeight())
                .build();
            
            order.addItem(orderItem);
        }

        // Calculate totals
        order.calculateTotals();

        // Save order
        order = orderRepository.save(order);
        log.info("[v0] Order created successfully: {}", order.getOrderNumber());

        // Clear cart
        cartService.deactivateCart(userId);

        // Send real-time notification via WebSocket
        webSocketService.sendOrderStatusUpdate(
            order.getId(),
            order.getStatus().name(),
            "Order placed successfully"
        );

        return order;
    }

    /**
     * Update order status
     */
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        log.info("[v0] Updating order {} to status: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setStatus(newStatus);
        
        if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setActualDeliveryTime(LocalDateTime.now());
        }

        order = orderRepository.save(order);

        // Send WebSocket notification
        webSocketService.sendOrderStatusUpdate(
            orderId,
            newStatus.name(),
            "Order status updated to " + newStatus
        );

        return order;
    }

    /**
     * Progress order to next status
     */
    public Order progressOrder(Long orderId) {
        log.info("[v0] Progressing order: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.progressStatus();
        order = orderRepository.save(order);

        // Send WebSocket notification
        webSocketService.sendOrderStatusUpdate(
            orderId,
            order.getStatus().name(),
            "Order progressed to " + order.getStatus()
        );

        return order;
    }

    /**
     * Cancel order and restore inventory
     */
    @Retryable(
        retryFor = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Order cancelOrder(Long orderId) {
        log.info("[v0] Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.isCancellable()) {
            throw new IllegalStateException(
                "Order cannot be cancelled in current status: " + order.getStatus()
            );
        }

        // Restore inventory
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository
                .findByProductVariantIdAndShopIdWithLock(
                    item.getVariant().getId(),
                    order.getShop().getId()
                )
                .orElseThrow(() -> new IllegalStateException("Inventory not found"));

            inventory.releaseReserved(item.getQuantity());
            inventoryRepository.save(inventory);
            
            log.info("[v0] Restored {} units of product: {}", 
                     item.getQuantity(), 
                     item.getProductName());
        }

        // Update order status
        order.setStatus(Order.OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        // Send WebSocket notification
        webSocketService.sendOrderStatusUpdate(
            orderId,
            Order.OrderStatus.CANCELLED.name(),
            "Order cancelled successfully"
        );

        return order;
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Calculate delivery fee based on distance
     */
    private BigDecimal calculateDeliveryFee(Shop shop, Double destLat, Double destLng) {
        // Simple distance-based calculation
        // In production, integrate with real delivery service API
        
        double distance = calculateDistance(
            shop.getLatitude(), 
            shop.getLongitude(), 
            destLat, 
            destLng
        );

        // Base fee + per km charge
        BigDecimal baseFee = new BigDecimal("20.00");
        BigDecimal perKmCharge = new BigDecimal("10.00");
        
        BigDecimal distanceFee = perKmCharge.multiply(BigDecimal.valueOf(distance));
        
        return baseFee.add(distanceFee);
    }

    /**
     * Calculate distance using Haversine formula
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
