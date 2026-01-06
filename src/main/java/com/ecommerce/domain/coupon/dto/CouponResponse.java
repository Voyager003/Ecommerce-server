package com.ecommerce.domain.coupon.dto;

import com.ecommerce.domain.coupon.domain.Coupon;
import com.ecommerce.domain.coupon.domain.CouponType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CouponResponse {

    private Long id;
    private String name;
    private CouponType type;
    private Long discountValue;
    private Long maxDiscountAmount;
    private Long minOrderAmount;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean available;

    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .type(coupon.getType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minOrderAmount(coupon.getMinOrderAmount())
                .totalQuantity(coupon.getTotalQuantity())
                .remainingQuantity(coupon.getRemainingQuantity())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .available(coupon.isAvailable())
                .build();
    }
}
