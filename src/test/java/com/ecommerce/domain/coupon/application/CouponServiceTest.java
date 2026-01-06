package com.ecommerce.domain.coupon.application;

import com.ecommerce.domain.coupon.dao.CouponRepository;
import com.ecommerce.domain.coupon.dao.MemberCouponRepository;
import com.ecommerce.domain.coupon.domain.*;
import com.ecommerce.domain.coupon.exception.CouponException;
import com.ecommerce.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private MemberCouponRepository memberCouponRepository;

    @Nested
    @DisplayName("쿠폰 발급")
    class IssueCouponTest {

        @Test
        @DisplayName("쿠폰을 정상 발급한다")
        void issueCoupon_Success() {
            // given
            Long memberId = 1L;
            Long couponId = 1L;
            Coupon coupon = createAvailableCoupon(couponId);

            given(memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)).willReturn(false);
            given(couponRepository.findByIdWithLock(couponId)).willReturn(Optional.of(coupon));
            given(memberCouponRepository.save(any(MemberCoupon.class))).willAnswer(i -> i.getArgument(0));

            // when
            MemberCoupon memberCoupon = couponService.issueCoupon(memberId, couponId);

            // then
            assertThat(memberCoupon.getMemberId()).isEqualTo(memberId);
            assertThat(memberCoupon.getStatus()).isEqualTo(CouponStatus.AVAILABLE);
            verify(memberCouponRepository).save(any(MemberCoupon.class));
        }

        @Test
        @DisplayName("이미 발급받은 쿠폰은 중복 발급 불가")
        void issueCoupon_AlreadyIssued_ThrowsException() {
            // given
            Long memberId = 1L;
            Long couponId = 1L;

            given(memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(memberId, couponId))
                    .isInstanceOf(CouponException.class)
                    .satisfies(e -> {
                        CouponException ex = (CouponException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.COUPON_ALREADY_ISSUED);
                    });
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰은 발급 불가")
        void issueCoupon_NotFound_ThrowsException() {
            // given
            Long memberId = 1L;
            Long couponId = 999L;

            given(memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)).willReturn(false);
            given(couponRepository.findByIdWithLock(couponId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(memberId, couponId))
                    .isInstanceOf(CouponException.class)
                    .satisfies(e -> {
                        CouponException ex = (CouponException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.COUPON_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("만료된 쿠폰은 발급 불가")
        void issueCoupon_Expired_ThrowsException() {
            // given
            Long memberId = 1L;
            Long couponId = 1L;
            Coupon expiredCoupon = createExpiredCoupon(couponId);

            given(memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)).willReturn(false);
            given(couponRepository.findByIdWithLock(couponId)).willReturn(Optional.of(expiredCoupon));

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(memberId, couponId))
                    .isInstanceOf(CouponException.class)
                    .satisfies(e -> {
                        CouponException ex = (CouponException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.COUPON_NOT_AVAILABLE);
                    });
        }
    }

    @Nested
    @DisplayName("쿠폰 사용")
    class UseCouponTest {

        @Test
        @DisplayName("쿠폰을 사용한다")
        void useCoupon_Success() {
            // given
            Long memberCouponId = 1L;
            Long orderId = 100L;
            MemberCoupon memberCoupon = createMemberCoupon(memberCouponId);

            given(memberCouponRepository.findById(memberCouponId)).willReturn(Optional.of(memberCoupon));

            // when
            couponService.useCoupon(memberCouponId, orderId);

            // then
            assertThat(memberCoupon.getStatus()).isEqualTo(CouponStatus.USED);
            assertThat(memberCoupon.getUsedOrderId()).isEqualTo(orderId);
        }
    }

    @Nested
    @DisplayName("쿠폰 복원")
    class RestoreCouponTest {

        @Test
        @DisplayName("사용한 쿠폰을 복원한다")
        void restoreCoupon_Success() {
            // given
            Long orderId = 100L;
            MemberCoupon memberCoupon = createUsedMemberCoupon(orderId);

            given(memberCouponRepository.findByUsedOrderId(orderId)).willReturn(Optional.of(memberCoupon));

            // when
            couponService.restoreCoupon(orderId);

            // then
            assertThat(memberCoupon.getStatus()).isEqualTo(CouponStatus.AVAILABLE);
            assertThat(memberCoupon.getUsedOrderId()).isNull();
        }
    }

    private Coupon createAvailableCoupon(Long id) {
        Coupon coupon = Coupon.builder()
                .name("테스트 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
        ReflectionTestUtils.setField(coupon, "id", id);
        return coupon;
    }

    private Coupon createExpiredCoupon(Long id) {
        Coupon coupon = Coupon.builder()
                .name("만료 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000L)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().minusDays(1))
                .build();
        ReflectionTestUtils.setField(coupon, "id", id);
        return coupon;
    }

    private MemberCoupon createMemberCoupon(Long id) {
        Coupon coupon = createAvailableCoupon(1L);
        MemberCoupon memberCoupon = MemberCoupon.builder()
                .memberId(1L)
                .coupon(coupon)
                .build();
        ReflectionTestUtils.setField(memberCoupon, "id", id);
        return memberCoupon;
    }

    private MemberCoupon createUsedMemberCoupon(Long orderId) {
        MemberCoupon memberCoupon = createMemberCoupon(1L);
        memberCoupon.use(orderId);
        return memberCoupon;
    }
}
