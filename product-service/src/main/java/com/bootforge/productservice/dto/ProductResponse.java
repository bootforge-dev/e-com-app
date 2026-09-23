package com.bootforge.productservice.dto;

import com.bootforge.productservice.entity.ProductStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String category,
        String brand,
        ProductStatus status,
        LocalDateTime createdAt
) {
}
