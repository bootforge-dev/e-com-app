package com.bootforge.order.dto;

import lombok.Builder;

@Builder
public record ShippingAddressResponse(

        String addressLine1,

        String city,

        String state,

        String postalCode,

        String country
) {
}