package com.ecommerce.domain.product.domain;

import com.ecommerce.domain.model.BaseTimeEntity;
import com.ecommerce.domain.model.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "base_price", nullable = false))
    private Money basePrice;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "selling_price", nullable = false))
    private Money sellingPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> options = new ArrayList<>();

    @Column(nullable = false)
    private boolean hasOption = false;

    @Column(length = 500)
    private String thumbnailUrl;

    @Builder
    public Product(String name, String description, Money basePrice, Money sellingPrice,
                   Category category, String thumbnailUrl) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.sellingPrice = sellingPrice;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
        this.status = ProductStatus.DRAFT;
    }

    public void updateInfo(String name, String description, Money basePrice,
                          Money sellingPrice, String thumbnailUrl) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (basePrice != null) {
            this.basePrice = basePrice;
        }
        if (sellingPrice != null) {
            this.sellingPrice = sellingPrice;
        }
        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }

    public void changeCategory(Category category) {
        this.category = category;
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void markSoldOut() {
        this.status = ProductStatus.SOLD_OUT;
    }

    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }

    public void addOption(ProductOption option) {
        this.options.add(option);
        this.hasOption = true;
    }

    public void removeOption(ProductOption option) {
        this.options.remove(option);
        if (this.options.isEmpty()) {
            this.hasOption = false;
        }
    }

    public boolean isPurchasable() {
        return this.status == ProductStatus.ACTIVE;
    }

    public Money getPrice() {
        return this.sellingPrice;
    }

    public long getDiscountRate() {
        if (basePrice.isZero() || basePrice.equals(sellingPrice)) {
            return 0;
        }
        long discount = basePrice.getAmount() - sellingPrice.getAmount();
        return (discount * 100) / basePrice.getAmount();
    }
}
