package com.ecommerce.domain.coupon.dto;

import com.ecommerce.domain.coupon.domain.Coupon;
import com.ecommerce.domain.coupon.domain.CouponType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CouponCreateRequest {

    @NotBlank(message = "쿠폰명은 필수입니다")
    @Size(max = 100, message = "쿠폰명은 100자 이하여야 합니다")
    private String name;

    @NotNull(message = "쿠폰 유형은 필수입니다")
    private CouponType type;

    @NotNull(message = "할인 값은 필수입니다")
    @Min(value = 1, message = "할인 값은 1 이상이어야 합니다")
    private Long discountValue;

    private Long maxDiscountAmount;

    @Min(value = 0, message = "최소 주문 금액은 0 이상이어야 합니다")
    private Long minOrderAmount = 0L;

    private Integer totalQuantity;

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    public Coupon toEntity() {
        return Coupon.builder()
                .name(name)
                .type(type)
                .discountValue(discountValue)
                .maxDiscountAmount(maxDiscountAmount)
                .minOrderAmount(minOrderAmount)
                .totalQuantity(totalQuantity)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
