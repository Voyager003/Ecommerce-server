package com.ecommerce.domain.order.domain;

import java.util.Arrays;
import java.util.List;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PREPARING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUND_REQUESTED,
    REFUNDED;

    public boolean canTransitionTo(OrderStatus next) {
        return getAllowedTransitions().contains(next);
    }

    private List<OrderStatus> getAllowedTransitions() {
        return switch (this) {
            case PENDING_PAYMENT -> Arrays.asList(PAID, CANCELLED);
            case PAID -> Arrays.asList(PREPARING, CANCELLED, REFUND_REQUESTED);
            case PREPARING -> Arrays.asList(SHIPPED, CANCELLED, REFUND_REQUESTED);
            case SHIPPED -> Arrays.asList(DELIVERED, REFUND_REQUESTED);
            case DELIVERED -> Arrays.asList(REFUND_REQUESTED);
            case REFUND_REQUESTED -> Arrays.asList(REFUNDED, DELIVERED);
            case CANCELLED, REFUNDED -> Arrays.asList();
        };
    }

    public boolean isCancellable() {
        return this == PENDING_PAYMENT || this == PAID || this == PREPARING;
    }

    public boolean isPending() {
        return this == PENDING_PAYMENT;
    }

    public boolean isCompleted() {
        return this == DELIVERED;
    }

    public boolean isFinal() {
        return this == CANCELLED || this == REFUNDED || this == DELIVERED;
    }
}
