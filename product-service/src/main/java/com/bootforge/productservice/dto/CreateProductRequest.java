package com.bootforge.productservice.dto;

import java.math.BigDecimal;

public record CreateProductRequest(
        String sku,
        String name,
        String description,
        BigDecimal price,
        String category,
        String brand
) {
}
