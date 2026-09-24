package com.bootforge.order.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponse(

        Long orderId,

        Long customerId,

        OrderStatus status,

        BigDecimal totalAmount,

        String currency,

        List<OrderItemResponse> items,

        ShippingAddressResponse shippingAddress,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}