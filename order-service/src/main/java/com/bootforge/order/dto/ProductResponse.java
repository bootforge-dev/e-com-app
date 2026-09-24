package com.bootforge.order.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String category,
        String brand,
        String status
) {
}
