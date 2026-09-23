package com.bootforge.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateInventoryRequest(
        @NotNull(message = "Quantity must be required")
        @Positive(message = "Quantity must not be negative")
        Integer quantity
) {
}
