package com.bootforge.inventory.dto;

public record InventoryAvailabilityResponse(
        Long productId,
        Integer requestedQuantity,
        Integer availableQuantity,
        boolean available
) {
}