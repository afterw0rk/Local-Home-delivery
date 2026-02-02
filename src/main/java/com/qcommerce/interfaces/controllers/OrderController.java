package com.qcommerce.interfaces.controllers;

import com.qcommerce.application.services.EnhancedOrderService;
import com.qcommerce.domain.entities.Order;
import com.qcommerce.infrastructure.persistence.OrderRepository;
import com.qcommerce.interfaces.dto.OrderPlacementRequest;
import com.qcommerce.interfaces.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Order Controller - Handles order operations
 * 
 * @apiNote Demonstrates transactional order processing with optimistic locking
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management APIs")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final EnhancedOrderService orderService;
    private final OrderRepository orderRepository;

    /**
     * Place a new order from cart
     * 
     * @apiNote This endpoint uses optimistic locking with automatic retry
     *          to handle concurrent orders during flash sales
     */
    @PostMapping
    @Operation(summary = "Place order", 
               description = "Create order from cart with inventory reservation and optimistic locking")
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody OrderPlacementRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("[v0] Place order request received for user: {}", userId);

        Order order = orderService.placeOrder(
            userId,
            request.getDeliveryAddress(),
            request.getDeliveryLatitude(),
            request.getDeliveryLongitude(),
            request.getCustomerNotes()
        );

        OrderResponse response = mapToOrderResponse(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("[v0] Get order request: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Verify user owns this order
        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        OrderResponse response = mapToOrderResponse(order);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user's orders
     */
    @GetMapping("/my-orders")
    @Operation(summary = "Get user orders", 
               description = "Retrieve paginated list of user's orders")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestHeader("X-User-Id") Long userId,
            Pageable pageable) {
        
        log.info("[v0] Get my orders request for user: {}", userId);

        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        Page<OrderResponse> response = orders.map(this::mapToOrderResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", 
               description = "Cancel order and restore inventory with optimistic locking")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("[v0] Cancel order request: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Verify user owns this order
        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Order cancelledOrder = orderService.cancelOrder(orderId);
        OrderResponse response = mapToOrderResponse(cancelledOrder);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update order status (shop/admin only)
     */
    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order status", 
               description = "Progress order through fulfillment stages")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        
        log.info("[v0] Update order status: orderId={}, status={}", orderId, status);

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        Order updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        
        OrderResponse response = mapToOrderResponse(updatedOrder);
        return ResponseEntity.ok(response);
    }

    /**
     * Progress order to next status
     */
    @PostMapping("/{orderId}/progress")
    @Operation(summary = "Progress order", 
               description = "Move order to next status in workflow")
    public ResponseEntity<OrderResponse> progressOrder(@PathVariable Long orderId) {
        log.info("[v0] Progress order request: orderId={}", orderId);

        Order order = orderService.progressOrder(orderId);
        OrderResponse response = mapToOrderResponse(order);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Map Order entity to response DTO
     */
    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .userId(order.getUserId())
            .shopId(order.getShop().getId())
            .shopName(order.getShop().getName())
            .status(order.getStatus().name())
            .subtotal(order.getSubtotal())
            .deliveryFee(order.getDeliveryFee())
            .totalAmount(order.getTotalAmount())
            .deliveryAddress(order.getDeliveryAddress())
            .customerNotes(order.getCustomerNotes())
            .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
            .actualDeliveryTime(order.getActualDeliveryTime())
            .createdAt(order.getCreatedAt())
            .build();
    }
}
