package com.ecommerce.domain.coupon.dto;

import com.ecommerce.domain.coupon.domain.CouponType;
import com.ecommerce.domain.coupon.domain.MemberCoupon;
import com.ecommerce.domain.model.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ApplicableCouponResponse {

    private Long memberCouponId;
    private Long couponId;
    private String couponName;
    private CouponType couponType;
    private Long discountValue;
    private Long maxDiscountAmount;
    private Long expectedDiscountAmount;
    private LocalDate endDate;

    public static ApplicableCouponResponse from(MemberCoupon memberCoupon, Money orderAmount) {
        Money discount = memberCoupon.getCoupon().calculateDiscount(orderAmount);

        return ApplicableCouponResponse.builder()
                .memberCouponId(memberCoupon.getId())
                .couponId(memberCoupon.getCouponId())
                .couponName(memberCoupon.getCouponName())
                .couponType(memberCoupon.getCouponType())
                .discountValue(memberCoupon.getCoupon().getDiscountValue())
                .maxDiscountAmount(memberCoupon.getCoupon().getMaxDiscountAmount())
                .expectedDiscountAmount(discount.getAmount())
                .endDate(memberCoupon.getCoupon().getEndDate())
                .build();
    }
}
