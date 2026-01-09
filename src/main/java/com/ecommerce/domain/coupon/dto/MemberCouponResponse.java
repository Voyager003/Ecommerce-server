package com.ecommerce.domain.coupon.dto;

import com.ecommerce.domain.coupon.domain.CouponStatus;
import com.ecommerce.domain.coupon.domain.CouponType;
import com.ecommerce.domain.coupon.domain.MemberCoupon;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MemberCouponResponse {

    private Long id;
    private Long couponId;
    private String couponName;
    private CouponType couponType;
    private Long discountValue;
    private Long maxDiscountAmount;
    private Long minOrderAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private CouponStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private boolean usable;

    public static MemberCouponResponse from(MemberCoupon memberCoupon) {
        return MemberCouponResponse.builder()
                .id(memberCoupon.getId())
                .couponId(memberCoupon.getCouponId())
                .couponName(memberCoupon.getCouponName())
                .couponType(memberCoupon.getCouponType())
                .discountValue(memberCoupon.getCoupon().getDiscountValue())
                .maxDiscountAmount(memberCoupon.getCoupon().getMaxDiscountAmount())
                .minOrderAmount(memberCoupon.getCoupon().getMinOrderAmount())
                .startDate(memberCoupon.getCoupon().getStartDate())
                .endDate(memberCoupon.getCoupon().getEndDate())
                .status(memberCoupon.getStatus())
                .issuedAt(memberCoupon.getIssuedAt())
                .usedAt(memberCoupon.getUsedAt())
                .usable(memberCoupon.isUsable())
                .build();
    }
}
