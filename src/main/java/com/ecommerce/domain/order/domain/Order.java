package com.ecommerce.domain.order.domain;

import com.ecommerce.domain.model.BaseTimeEntity;
import com.ecommerce.domain.model.Money;
import com.ecommerce.domain.order.exception.OrderException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(nullable = false)
    private Long memberId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false))
    private Money totalAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "discount_amount", nullable = false))
    private Money discountAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "delivery_fee", nullable = false))
    private Money deliveryFee;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "final_amount", nullable = false))
    private Money finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Embedded
    private ShippingInfo shippingInfo;

    private Long couponId;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    @Builder
    public Order(Long memberId, ShippingInfo shippingInfo, Long couponId) {
        this.orderNumber = generateOrderNumber();
        this.memberId = memberId;
        this.shippingInfo = shippingInfo;
        this.couponId = couponId;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.totalAmount = Money.ZERO;
        this.discountAmount = Money.ZERO;
        this.deliveryFee = Money.of(3000L);
        this.finalAmount = Money.ZERO;
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        recalculateAmounts();
    }

    public void recalculateAmounts() {
        this.totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.ZERO, Money::add);

        Money freeDeliveryThreshold = Money.of(50000L);
        if (this.totalAmount.isGreaterThanOrEqual(freeDeliveryThreshold)) {
            this.deliveryFee = Money.ZERO;
        } else {
            this.deliveryFee = Money.of(3000L);
        }

        this.finalAmount = this.totalAmount
                .subtract(this.discountAmount)
                .add(this.deliveryFee);
    }

    public void applyDiscount(Money discountAmount) {
        this.discountAmount = discountAmount;
        recalculateAmounts();
    }

    public void markAsPaid() {
        validateStatusTransition(OrderStatus.PAID);
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void startPreparing() {
        validateStatusTransition(OrderStatus.PREPARING);
        this.status = OrderStatus.PREPARING;
    }

    public void ship() {
        validateStatusTransition(OrderStatus.SHIPPED);
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        validateStatusTransition(OrderStatus.DELIVERED);
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (!this.status.isCancellable()) {
            throw OrderException.cannotCancel();
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void requestRefund() {
        validateStatusTransition(OrderStatus.REFUND_REQUESTED);
        this.status = OrderStatus.REFUND_REQUESTED;
    }

    public void completeRefund() {
        validateStatusTransition(OrderStatus.REFUNDED);
        this.status = OrderStatus.REFUNDED;
    }

    private void validateStatusTransition(OrderStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw OrderException.invalidStatus();
        }
    }

    public boolean isPending() {
        return this.status.isPending();
    }

    public boolean isCancellable() {
        return this.status.isCancellable();
    }
}
