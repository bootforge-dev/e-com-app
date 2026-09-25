package com.bootforge.payment.service;

import com.bootforge.payment.dto.CreatePaymentRequest;
import com.bootforge.payment.dto.PageResponse;
import com.bootforge.payment.dto.PaymentResponse;
import com.bootforge.payment.entity.Payment;
import com.bootforge.payment.enums.PaymentStatus;
import com.bootforge.payment.exception.InvalidPaymentException;
import com.bootforge.payment.exception.PaymentNotFoundException;
import com.bootforge.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        if (paymentRepository.existsByOrderId(request.orderId())) {
            throw new InvalidPaymentException("Payment already exists for orderId: " + request.orderId());
        }
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .customerId(request.customerId())
                .amount(request.amount())
                .currency(request.currency() == null
                        ? "INR"
                        : request.currency())
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return toPaymentResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        return toPaymentResponse(getPayment(paymentId));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new PaymentNotFoundException("Payment not found for orderId: " + orderId)
        );

        return toPaymentResponse(payment);
    }


    @Transactional
    public PaymentResponse processPayment(Long paymentId) {
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentException("Payment cannot be processed in status: " + payment.getStatus());
        }
        payment.setStatus(PaymentStatus.PROCESSING);

        //simulate payment provider call
        boolean paymentSuccessful = simulatePayment();

        if (paymentSuccessful) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId("TXN-" + UUID.randomUUID());
            payment.setFailureReason(null);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment processing failed");
        }
        return toPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentException("Only successful payments can be refunded");
        }
        //simulate refund
        payment.setStatus(PaymentStatus.REFUNDED);
        return toPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        Payment payment = getPayment(paymentId);
        if(payment.getStatus() == PaymentStatus.SUCCESS){
            throw new InvalidPaymentException("Successful payments cannot be cancelled. User refund instead.");
        }
        if(payment.getStatus() == PaymentStatus.REFUNDED){
            throw new InvalidPaymentException("Payment already refunded");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        return toPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getPayments(Pageable pageable) {
        Page<Payment> paymentPage = paymentRepository.findAll(pageable);
        List<PaymentResponse> data = paymentPage.getContent().stream().map(this::toPaymentResponse).toList();

        return new PageResponse<>(
                data,
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages()
        );
    }

    private Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(
                () -> new PaymentNotFoundException("Payment not found for paymentID: " + paymentId)
        );
    }

    private boolean simulatePayment() {
        return true;
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
