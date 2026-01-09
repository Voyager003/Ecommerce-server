package com.ecommerce.domain.member.dto;

import com.ecommerce.domain.member.application.GradeBenefitProvider;
import com.ecommerce.domain.member.domain.MemberGrade;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GradeBenefitResponse {

    private MemberGrade currentGrade;
    private int pointRate;
    private boolean freeShipping;
    private int shippingFee;
    private MemberGrade nextGrade;
    private Long amountToNextGrade;
    private String gradeDescription;
    private String nextGradeDescription;

    public static GradeBenefitResponse from(GradeBenefitProvider.GradeBenefit benefit) {
        return GradeBenefitResponse.builder()
                .currentGrade(benefit.grade())
                .pointRate(benefit.pointRate())
                .freeShipping(benefit.freeShipping())
                .shippingFee(benefit.shippingFee())
                .nextGrade(benefit.nextGrade())
                .amountToNextGrade(benefit.amountToNextGrade())
                .gradeDescription(getGradeDescription(benefit.grade()))
                .nextGradeDescription(benefit.nextGrade() != null ?
                        getGradeDescription(benefit.nextGrade()) : null)
                .build();
    }

    private static String getGradeDescription(MemberGrade grade) {
        return switch (grade) {
            case BRONZE -> "브론즈 회원";
            case SILVER -> "실버 회원 - 구매금액 10만원 이상";
            case GOLD -> "골드 회원 - 구매금액 30만원 이상, 무료배송";
            case PLATINUM -> "플래티넘 회원 - 구매금액 50만원 이상, 무료배송";
        };
    }
}
