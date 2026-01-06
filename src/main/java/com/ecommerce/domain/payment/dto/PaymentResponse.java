package com.ecommerce.domain.payment.dto;

import com.ecommerce.domain.payment.domain.Payment;
import com.ecommerce.domain.payment.domain.PaymentMethod;
import com.ecommerce.domain.payment.domain.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {

    private Long id;
    private String paymentNumber;
    private Long orderId;
    private long amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String pgTransactionId;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount().getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .pgTransactionId(payment.getPgTransactionId())
                .approvedAt(payment.getApprovedAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
