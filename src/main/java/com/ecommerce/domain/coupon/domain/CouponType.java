package com.ecommerce.domain.coupon.domain;

public enum CouponType {
    FIXED_AMOUNT("정액 할인"),
    PERCENTAGE("정률 할인"),
    FREE_SHIPPING("무료 배송");

    private final String description;

    CouponType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
