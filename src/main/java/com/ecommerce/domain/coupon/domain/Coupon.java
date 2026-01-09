package com.ecommerce.domain.coupon.domain;

import com.ecommerce.domain.coupon.exception.CouponException;
import com.ecommerce.domain.model.BaseTimeEntity;
import com.ecommerce.domain.model.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponType type;

    @Column(nullable = false)
    private Long discountValue;

    private Long maxDiscountAmount;

    @Column(nullable = false)
    private Long minOrderAmount = 0L;

    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer issuedQuantity = 0;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean isActive = true;

    @Version
    private Long version;

    @Builder
    public Coupon(String name, CouponType type, Long discountValue, Long maxDiscountAmount,
                  Long minOrderAmount, Integer totalQuantity, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        validateDiscountValue(type, discountValue);

        this.name = name;
        this.type = type;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderAmount = minOrderAmount != null ? minOrderAmount : 0L;
        this.totalQuantity = totalQuantity;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다");
        }
    }

    private void validateDiscountValue(CouponType type, Long discountValue) {
        if (discountValue <= 0) {
            throw new IllegalArgumentException("할인 값은 0보다 커야 합니다");
        }
        if (type == CouponType.PERCENTAGE && discountValue > 100) {
            throw new IllegalArgumentException("정률 할인은 100%를 초과할 수 없습니다");
        }
    }

    public boolean isAvailable() {
        LocalDate today = LocalDate.now();
        return isActive && !today.isBefore(startDate) && !today.isAfter(endDate) && hasRemainingQuantity();
    }

    public boolean hasRemainingQuantity() {
        return totalQuantity == null || issuedQuantity < totalQuantity;
    }

    public int getRemainingQuantity() {
        if (totalQuantity == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, totalQuantity - issuedQuantity);
    }

    public void issue() {
        if (!isAvailable()) {
            throw CouponException.notAvailable();
        }
        this.issuedQuantity++;
    }

    public void cancelIssue() {
        if (this.issuedQuantity > 0) {
            this.issuedQuantity--;
        }
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public Money calculateDiscount(Money orderAmount) {
        if (!meetsMinOrderAmount(orderAmount)) {
            return Money.ZERO;
        }

        return switch (type) {
            case FIXED_AMOUNT -> Money.of(discountValue);
            case PERCENTAGE -> {
                Money discount = orderAmount.multiply(discountValue).divide(100);
                if (maxDiscountAmount != null) {
                    Money maxDiscount = Money.of(maxDiscountAmount);
                    yield discount.compareTo(maxDiscount) > 0 ? maxDiscount : discount;
                }
                yield discount;
            }
            case FREE_SHIPPING -> Money.ZERO;
        };
    }

    public boolean meetsMinOrderAmount(Money orderAmount) {
        return orderAmount.compareTo(Money.of(minOrderAmount)) >= 0;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }
}
