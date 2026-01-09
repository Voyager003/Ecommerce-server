package com.ecommerce.domain.member.application;

import com.ecommerce.domain.member.domain.MemberGrade;
import com.ecommerce.domain.model.Money;
import org.springframework.stereotype.Component;

@Component
public class GradeBenefitProvider {

    private static final Money FREE_SHIPPING_THRESHOLD = Money.of(50000L);

    public int getPointRate(MemberGrade grade) {
        return grade.getPointRate();
    }

    public Money calculatePoints(MemberGrade grade, Money orderAmount) {
        int rate = grade.getPointRate();
        return orderAmount.multiply(rate).divide(100);
    }

    public Money getShippingFee(MemberGrade grade, Money orderAmount) {
        if (grade.isFreeShipping()) {
            return Money.ZERO;
        }
        if (orderAmount.isGreaterThanOrEqual(FREE_SHIPPING_THRESHOLD)) {
            return Money.ZERO;
        }
        return Money.of(grade.getShippingFee());
    }

    public boolean isFreeShipping(MemberGrade grade) {
        return grade.isFreeShipping();
    }

    public Money getFreeShippingThreshold() {
        return FREE_SHIPPING_THRESHOLD;
    }

    public GradeBenefit getBenefits(MemberGrade grade) {
        return new GradeBenefit(
                grade,
                grade.getPointRate(),
                grade.isFreeShipping(),
                grade.getShippingFee(),
                getNextGrade(grade),
                getAmountToNextGrade(grade, 0L)
        );
    }

    public GradeBenefit getBenefitsWithProgress(MemberGrade grade, long currentPurchaseAmount) {
        return new GradeBenefit(
                grade,
                grade.getPointRate(),
                grade.isFreeShipping(),
                grade.getShippingFee(),
                getNextGrade(grade),
                getAmountToNextGrade(grade, currentPurchaseAmount)
        );
    }

    private MemberGrade getNextGrade(MemberGrade currentGrade) {
        return switch (currentGrade) {
            case BRONZE -> MemberGrade.SILVER;
            case SILVER -> MemberGrade.GOLD;
            case GOLD -> MemberGrade.PLATINUM;
            case PLATINUM -> null;
        };
    }

    private Long getAmountToNextGrade(MemberGrade currentGrade, long currentPurchaseAmount) {
        MemberGrade nextGrade = getNextGrade(currentGrade);
        if (nextGrade == null) {
            return null;
        }
        long remaining = nextGrade.getMinPurchaseAmount() - currentPurchaseAmount;
        return Math.max(0, remaining);
    }

    public record GradeBenefit(
            MemberGrade grade,
            int pointRate,
            boolean freeShipping,
            int shippingFee,
            MemberGrade nextGrade,
            Long amountToNextGrade
    ) {}
}
