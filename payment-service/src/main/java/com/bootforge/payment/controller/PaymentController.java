package com.bootforge.payment.controller;

import com.bootforge.payment.dto.CreatePaymentRequest;
import com.bootforge.payment.dto.PageResponse;
import com.bootforge.payment.dto.PaymentResponse;
import com.bootforge.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(
        name = "Payment",
        description = "Payment management APIs"
)
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create payment")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest paymentRequest){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(paymentRequest));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(@PathVariable Long orderId){
        return ResponseEntity.ok(paymentService.getPaymentByOrder(orderId));
    }

    @PostMapping("/{paymentId}/process")
    @Operation(summary = "Process payment")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long paymentId){
        return ResponseEntity.ok(paymentService.processPayment(paymentId));
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund payment")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId){
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }

    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel payment")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long paymentId){
        return ResponseEntity.ok(paymentService.cancelPayment(paymentId));
    }

    @GetMapping
    @Operation(summary = "Get all payments")
    public ResponseEntity<PageResponse<PaymentResponse>> getAllPayments(
            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable){
        return ResponseEntity.ok(paymentService.getPayments(pageable));
    }
}
