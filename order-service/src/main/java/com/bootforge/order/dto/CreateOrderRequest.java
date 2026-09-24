package com.bootforge.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateOrderRequest(
        @NotNull(message = "Customer id must be required")
        Long customerId,

        @NotNull(message = "Items not be zero")
        List<@Valid OrderItemRequest> items,

        @NotNull(message = "Shipping address must be required")
        @Valid
        ShippingAddressRequest shippingAddress
) {
}
