package com.ecommerce.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberGrade {
    BRONZE(0L, 0, 3000),
    SILVER(100_000L, 1, 3000),
    GOLD(300_000L, 3, 0),
    PLATINUM(500_000L, 5, 0);

    private final long minPurchaseAmount;
    private final int pointRate;
    private final int shippingFee;

    public boolean isFreeShipping() {
        return shippingFee == 0;
    }

    public static MemberGrade calculateGrade(long totalPurchaseAmount) {
        if (totalPurchaseAmount >= PLATINUM.minPurchaseAmount) {
            return PLATINUM;
        } else if (totalPurchaseAmount >= GOLD.minPurchaseAmount) {
            return GOLD;
        } else if (totalPurchaseAmount >= SILVER.minPurchaseAmount) {
            return SILVER;
        }
        return BRONZE;
    }
}
