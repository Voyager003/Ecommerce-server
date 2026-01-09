package com.ecommerce.domain.coupon.domain;

import com.ecommerce.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @Nested
    @DisplayName("쿠폰 생성")
    class CreateTest {

        @Test
        @DisplayName("정액 할인 쿠폰을 생성한다")
        void createFixedAmountCoupon_Success() {
            // given & when
            Coupon coupon = createFixedAmountCoupon(5000L);

            // then
            assertThat(coupon.getName()).isEqualTo("테스트 쿠폰");
            assertThat(coupon.getType()).isEqualTo(CouponType.FIXED_AMOUNT);
            assertThat(coupon.getDiscountValue()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("정률 할인 쿠폰을 생성한다")
        void createPercentageCoupon_Success() {
            // given & when
            Coupon coupon = createPercentageCoupon(10L, 10000L);

            // then
            assertThat(coupon.getType()).isEqualTo(CouponType.PERCENTAGE);
            assertThat(coupon.getDiscountValue()).isEqualTo(10L);
            assertThat(coupon.getMaxDiscountAmount()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("정률 할인은 100%를 초과할 수 없다")
        void createPercentageCoupon_Exceeds100_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> createPercentageCoupon(101L, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
        void createCoupon_InvalidDates_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> Coupon.builder()
                    .name("테스트 쿠폰")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(5000L)
                    .startDate(LocalDate.now().plusDays(10))
                    .endDate(LocalDate.now())
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("할인 금액 계산")
    class CalculateDiscountTest {

        @Test
        @DisplayName("정액 할인을 계산한다")
        void calculateDiscount_FixedAmount() {
            // given
            Coupon coupon = createFixedAmountCoupon(5000L);
            Money orderAmount = Money.of(50000L);

            // when
            Money discount = coupon.calculateDiscount(orderAmount);

            // then
            assertThat(discount.getAmount()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("정률 할인을 계산한다")
        void calculateDiscount_Percentage() {
            // given
            Coupon coupon = createPercentageCoupon(10L, null);
            Money orderAmount = Money.of(50000L);

            // when
            Money discount = coupon.calculateDiscount(orderAmount);

            // then
            assertThat(discount.getAmount()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("정률 할인은 최대 할인 금액을 초과하지 않는다")
        void calculateDiscount_Percentage_WithMax() {
            // given
            Coupon coupon = createPercentageCoupon(20L, 5000L);
            Money orderAmount = Money.of(50000L);

            // when
            Money discount = coupon.calculateDiscount(orderAmount);

            // then
            assertThat(discount.getAmount()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("최소 주문 금액 미달 시 할인 0원")
        void calculateDiscount_BelowMinOrder() {
            // given
            Coupon coupon = Coupon.builder()
                    .name("테스트 쿠폰")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(5000L)
                    .minOrderAmount(30000L)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(30))
                    .build();
            Money orderAmount = Money.of(20000L);

            // when
            Money discount = coupon.calculateDiscount(orderAmount);

            // then
            assertThat(discount.getAmount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("쿠폰 발급")
    class IssueTest {

        @Test
        @DisplayName("쿠폰을 발급하면 발급 수량이 증가한다")
        void issue_IncreasesIssuedQuantity() {
            // given
            Coupon coupon = createLimitedCoupon(100);

            // when
            coupon.issue();

            // then
            assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
            assertThat(coupon.getRemainingQuantity()).isEqualTo(99);
        }

        @Test
        @DisplayName("무제한 쿠폰은 계속 발급 가능하다")
        void issue_Unlimited_AlwaysAvailable() {
            // given
            Coupon coupon = createUnlimitedCoupon();

            // when
            for (int i = 0; i < 1000; i++) {
                coupon.issue();
            }

            // then
            assertThat(coupon.hasRemainingQuantity()).isTrue();
            assertThat(coupon.getRemainingQuantity()).isEqualTo(Integer.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("쿠폰 유효성")
    class AvailabilityTest {

        @Test
        @DisplayName("유효기간 내 활성 쿠폰은 사용 가능하다")
        void isAvailable_ActiveAndValid() {
            // given
            Coupon coupon = createFixedAmountCoupon(5000L);

            // then
            assertThat(coupon.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("비활성 쿠폰은 사용 불가하다")
        void isAvailable_Inactive() {
            // given
            Coupon coupon = createFixedAmountCoupon(5000L);
            coupon.deactivate();

            // then
            assertThat(coupon.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("만료된 쿠폰은 사용 불가하다")
        void isAvailable_Expired() {
            // given
            Coupon coupon = Coupon.builder()
                    .name("만료 쿠폰")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(5000L)
                    .startDate(LocalDate.now().minusDays(30))
                    .endDate(LocalDate.now().minusDays(1))
                    .build();

            // then
            assertThat(coupon.isAvailable()).isFalse();
            assertThat(coupon.isExpired()).isTrue();
        }
    }

    private Coupon createFixedAmountCoupon(Long amount) {
        return Coupon.builder()
                .name("테스트 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(amount)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
    }

    private Coupon createPercentageCoupon(Long percentage, Long maxDiscount) {
        return Coupon.builder()
                .name("테스트 쿠폰")
                .type(CouponType.PERCENTAGE)
                .discountValue(percentage)
                .maxDiscountAmount(maxDiscount)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
    }

    private Coupon createLimitedCoupon(int quantity) {
        return Coupon.builder()
                .name("테스트 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000L)
                .totalQuantity(quantity)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
    }

    private Coupon createUnlimitedCoupon() {
        return Coupon.builder()
                .name("무제한 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000L)
                .totalQuantity(null)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
    }
}
