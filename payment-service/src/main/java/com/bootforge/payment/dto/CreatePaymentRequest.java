package com.bootforge.payment.dto;

import com.bootforge.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreatePaymentRequest(
        @NotNull(message = "Order Id is required")
        Long orderId,

        @NotNull(message = "Customer Id is required")
        Long customerId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount should not be negative")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        String currency
) {
}
