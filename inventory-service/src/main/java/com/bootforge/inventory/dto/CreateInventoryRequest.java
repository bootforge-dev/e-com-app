package com.bootforge.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record CreateInventoryRequest(

        @NotNull(message = "Product Id must be required")
        @Positive(message = "Product not negative")
        Long productId,

        @NotNull(message = "Quantity must be required")
        @Positive(message = "Quantity must not be negative")
        Integer quantity
) {
}
