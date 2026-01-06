package com.ecommerce.domain.payment.domain;

public enum PaymentStatus {
    PENDING,
    APPROVED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED;

    public boolean isSuccess() {
        return this == APPROVED;
    }

    public boolean isCancellable() {
        return this == APPROVED;
    }

    public boolean isRefundable() {
        return this == APPROVED || this == PARTIALLY_REFUNDED;
    }

    public boolean isFinal() {
        return this == FAILED || this == REFUNDED;
    }
}
