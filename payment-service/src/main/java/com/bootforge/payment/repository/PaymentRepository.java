package com.bootforge.payment.repository;

import com.bootforge.payment.entity.Payment;
import com.bootforge.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
