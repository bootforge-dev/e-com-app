package com.bootforge.payment.dto;

import com.bootforge.payment.enums.PaymentMethod;
import com.bootforge.payment.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentResponse(

        Long paymentId,
        Long orderId,
        Long customerId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String transactionId,
        String failureReason,
        LocalDateTime createdAt
) {
}