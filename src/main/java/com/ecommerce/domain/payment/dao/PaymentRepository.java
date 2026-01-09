package com.ecommerce.domain.payment.dao;

import com.ecommerce.domain.payment.domain.Payment;
import com.ecommerce.domain.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
