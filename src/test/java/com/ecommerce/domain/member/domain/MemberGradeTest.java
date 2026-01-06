package com.ecommerce.domain.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MemberGradeTest {

    @Nested
    @DisplayName("등급 계산")
    class CalculateGradeTest {

        @ParameterizedTest
        @DisplayName("구매 금액에 따라 등급이 결정된다")
        @CsvSource({
                "0, BRONZE",
                "50000, BRONZE",
                "99999, BRONZE",
                "100000, SILVER",
                "200000, SILVER",
                "299999, SILVER",
                "300000, GOLD",
                "400000, GOLD",
                "499999, GOLD",
                "500000, PLATINUM",
                "1000000, PLATINUM"
        })
        void calculateGrade_ByPurchaseAmount(long amount, MemberGrade expectedGrade) {
            // when
            MemberGrade grade = MemberGrade.calculateGrade(amount);

            // then
            assertThat(grade).isEqualTo(expectedGrade);
        }
    }

    @Nested
    @DisplayName("등급별 혜택")
    class GradeBenefitsTest {

        @Test
        @DisplayName("브론즈 등급 혜택 - 적립률 0%, 배송비 3000원")
        void bronzeBenefits() {
            // given
            MemberGrade grade = MemberGrade.BRONZE;

            // then
            assertThat(grade.getPointRate()).isEqualTo(0);
            assertThat(grade.getShippingFee()).isEqualTo(3000);
            assertThat(grade.isFreeShipping()).isFalse();
        }

        @Test
        @DisplayName("실버 등급 혜택 - 적립률 1%, 배송비 3000원")
        void silverBenefits() {
            // given
            MemberGrade grade = MemberGrade.SILVER;

            // then
            assertThat(grade.getPointRate()).isEqualTo(1);
            assertThat(grade.getShippingFee()).isEqualTo(3000);
            assertThat(grade.isFreeShipping()).isFalse();
        }

        @Test
        @DisplayName("골드 등급 혜택 - 적립률 3%, 무료배송")
        void goldBenefits() {
            // given
            MemberGrade grade = MemberGrade.GOLD;

            // then
            assertThat(grade.getPointRate()).isEqualTo(3);
            assertThat(grade.getShippingFee()).isEqualTo(0);
            assertThat(grade.isFreeShipping()).isTrue();
        }

        @Test
        @DisplayName("플래티넘 등급 혜택 - 적립률 5%, 무료배송")
        void platinumBenefits() {
            // given
            MemberGrade grade = MemberGrade.PLATINUM;

            // then
            assertThat(grade.getPointRate()).isEqualTo(5);
            assertThat(grade.getShippingFee()).isEqualTo(0);
            assertThat(grade.isFreeShipping()).isTrue();
        }
    }

    @Nested
    @DisplayName("등급 기준 금액")
    class MinPurchaseAmountTest {

        @Test
        @DisplayName("각 등급의 기준 금액이 올바르다")
        void minPurchaseAmounts() {
            assertThat(MemberGrade.BRONZE.getMinPurchaseAmount()).isEqualTo(0L);
            assertThat(MemberGrade.SILVER.getMinPurchaseAmount()).isEqualTo(100_000L);
            assertThat(MemberGrade.GOLD.getMinPurchaseAmount()).isEqualTo(300_000L);
            assertThat(MemberGrade.PLATINUM.getMinPurchaseAmount()).isEqualTo(500_000L);
        }
    }
}
