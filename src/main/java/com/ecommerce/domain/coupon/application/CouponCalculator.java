package com.ecommerce.domain.coupon.application;

import com.ecommerce.domain.coupon.domain.Coupon;
import com.ecommerce.domain.coupon.domain.CouponType;
import com.ecommerce.domain.coupon.domain.MemberCoupon;
import com.ecommerce.domain.coupon.exception.CouponException;
import com.ecommerce.domain.model.Money;
import org.springframework.stereotype.Component;

@Component
public class CouponCalculator {

    public Money calculateDiscount(MemberCoupon memberCoupon, Money orderAmount) {
        if (!memberCoupon.isUsable()) {
            return Money.ZERO;
        }

        Coupon coupon = memberCoupon.getCoupon();

        if (!coupon.meetsMinOrderAmount(orderAmount)) {
            throw CouponException.minOrderAmountNotMet();
        }

        return coupon.calculateDiscount(orderAmount);
    }

    public Money calculateDiscountSafely(MemberCoupon memberCoupon, Money orderAmount) {
        if (!memberCoupon.isUsable()) {
            return Money.ZERO;
        }

        Coupon coupon = memberCoupon.getCoupon();

        if (!coupon.meetsMinOrderAmount(orderAmount)) {
            return Money.ZERO;
        }

        return coupon.calculateDiscount(orderAmount);
    }

    public boolean isApplicable(MemberCoupon memberCoupon, Money orderAmount) {
        if (!memberCoupon.isUsable()) {
            return false;
        }
        return memberCoupon.getCoupon().meetsMinOrderAmount(orderAmount);
    }

    public boolean isFreeShippingCoupon(MemberCoupon memberCoupon) {
        return memberCoupon.getCouponType() == CouponType.FREE_SHIPPING;
    }

    public Money calculateFinalAmount(Money orderAmount, Money deliveryFee, MemberCoupon memberCoupon) {
        if (memberCoupon == null) {
            return orderAmount.add(deliveryFee);
        }

        Coupon coupon = memberCoupon.getCoupon();

        if (coupon.getType() == CouponType.FREE_SHIPPING) {
            return orderAmount;
        }

        Money discount = calculateDiscountSafely(memberCoupon, orderAmount);
        Money discountedOrderAmount = orderAmount.subtract(discount);

        if (discountedOrderAmount.isLessThan(Money.ZERO)) {
            discountedOrderAmount = Money.ZERO;
        }

        return discountedOrderAmount.add(deliveryFee);
    }
}
