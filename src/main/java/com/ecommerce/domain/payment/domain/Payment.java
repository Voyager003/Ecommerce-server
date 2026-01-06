package com.ecommerce.domain.payment.domain;

import com.ecommerce.domain.model.BaseTimeEntity;
import com.ecommerce.domain.model.Money;
import com.ecommerce.domain.payment.exception.PaymentException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String paymentNumber;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long memberId;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false))
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 100)
    private String pgTransactionId;

    @Column(length = 50)
    private String idempotencyKey;

    private String failureReason;

    private LocalDateTime approvedAt;

    private LocalDateTime cancelledAt;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "refunded_amount"))
    private Money refundedAmount;

    @Builder
    public Payment(Long orderId, Long memberId, Money amount, PaymentMethod method, String idempotencyKey) {
        this.paymentNumber = generatePaymentNumber();
        this.orderId = orderId;
        this.memberId = memberId;
        this.amount = amount;
        this.method = method;
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.PENDING;
        this.refundedAmount = Money.ZERO;
    }

    private String generatePaymentNumber() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void approve(String pgTransactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw PaymentException.duplicatePayment();
        }
        this.status = PaymentStatus.APPROVED;
        this.pgTransactionId = pgTransactionId;
        this.approvedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void cancel() {
        if (!this.status.isCancellable()) {
            throw PaymentException.cannotCancel();
        }
        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.refundedAmount = this.amount;
    }

    public void refund(Money refundAmount) {
        if (!this.status.isRefundable()) {
            throw PaymentException.cannotCancel();
        }

        Money newTotalRefund = this.refundedAmount.add(refundAmount);
        if (newTotalRefund.isGreaterThanOrEqual(this.amount)) {
            this.refundedAmount = this.amount;
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.refundedAmount = newTotalRefund;
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    public boolean isApproved() {
        return this.status == PaymentStatus.APPROVED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public Money getRefundableAmount() {
        return this.amount.subtract(this.refundedAmount);
    }
}
