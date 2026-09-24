package com.bootforge.order.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemResponse(
        Long productId,

        String productName,

        String sku,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice
) {
}
