package com.bootforge.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record OrderItemRequest(
        @NotNull(message = "Product must be required")
        Long productId,

        @NotNull(message = "Quantity must be required")
        @Positive(message = "Quantity must be not negative")
        Integer quantity
) {
}
