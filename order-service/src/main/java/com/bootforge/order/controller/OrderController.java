package com.bootforge.order.controller;

import com.bootforge.order.dto.CreateOrderRequest;
import com.bootforge.order.dto.OrderResponse;
import com.bootforge.order.dto.PageResponse;
import com.bootforge.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(pageable));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer")
    public ResponseEntity<PageResponse<OrderResponse>> getOrdersByCustomer(
            @PathVariable Long customerId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrderByCustomer(customerId, pageable));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

}
