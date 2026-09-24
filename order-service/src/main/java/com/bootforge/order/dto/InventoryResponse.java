package com.bootforge.order.dto;

import lombok.Builder;

@Builder
public record InventoryResponse(
        Long id,
        Long productId,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity
) {
}