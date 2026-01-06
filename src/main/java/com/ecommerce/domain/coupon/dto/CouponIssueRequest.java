package com.ecommerce.domain.coupon.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponIssueRequest {

    @NotNull(message = "쿠폰 ID는 필수입니다")
    private Long couponId;
}
