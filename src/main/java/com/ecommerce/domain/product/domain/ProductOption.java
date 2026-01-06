package com.ecommerce.domain.product.domain;

import com.ecommerce.domain.model.BaseTimeEntity;
import com.ecommerce.domain.model.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String name;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "additional_price"))
    private Money additionalPrice;

    @Column(nullable = false)
    private boolean active = true;

    @Builder
    public ProductOption(Product product, String name, Money additionalPrice) {
        this.product = product;
        this.name = name;
        this.additionalPrice = additionalPrice != null ? additionalPrice : Money.ZERO;
        this.active = true;
    }

    public void update(String name, Money additionalPrice) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (additionalPrice != null) {
            this.additionalPrice = additionalPrice;
        }
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public Money getTotalPrice() {
        return product.getSellingPrice().add(additionalPrice);
    }
}
