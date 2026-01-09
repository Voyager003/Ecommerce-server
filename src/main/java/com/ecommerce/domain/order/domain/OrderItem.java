package com.ecommerce.domain.order.domain;

import com.ecommerce.domain.model.BaseTimeEntity;
import com.ecommerce.domain.model.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long productId;

    private Long productOptionId;

    @Column(nullable = false, length = 200)
    private String productName;

    private String optionName;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price", nullable = false))
    private Money unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "subtotal", nullable = false))
    private Money subtotal;

    @Builder
    public OrderItem(Long productId, Long productOptionId, String productName,
                    String optionName, Money unitPrice, int quantity) {
        this.productId = productId;
        this.productOptionId = productOptionId;
        this.productName = productName;
        this.optionName = optionName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice.multiply(quantity);
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public Money getSubtotal() {
        return this.subtotal;
    }
}
