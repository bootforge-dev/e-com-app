package com.bootforge.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ShippingAddressRequest(
        @NotBlank
        String addressLine1,

        @NotBlank
        String city,

        @NotBlank
        String state,

        @NotBlank
        String postalCode,

        @NotBlank
        String country
) {
}
