package com.ecommerce.domain.coupon.dao;

import com.ecommerce.domain.coupon.domain.CouponStatus;
import com.ecommerce.domain.coupon.domain.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    List<MemberCoupon> findByMemberId(Long memberId);

    List<MemberCoupon> findByMemberIdAndStatus(Long memberId, CouponStatus status);

    @Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon WHERE mc.memberId = :memberId AND mc.status = :status")
    List<MemberCoupon> findByMemberIdAndStatusWithCoupon(@Param("memberId") Long memberId,
                                                          @Param("status") CouponStatus status);

    @Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon WHERE mc.memberId = :memberId")
    List<MemberCoupon> findByMemberIdWithCoupon(@Param("memberId") Long memberId);

    Optional<MemberCoupon> findByMemberIdAndCouponId(Long memberId, Long couponId);

    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);

    @Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon " +
            "WHERE mc.memberId = :memberId AND mc.status = 'AVAILABLE' " +
            "AND mc.coupon.minOrderAmount <= :orderAmount " +
            "AND mc.coupon.endDate >= CURRENT_DATE")
    List<MemberCoupon> findApplicableCoupons(@Param("memberId") Long memberId,
                                              @Param("orderAmount") Long orderAmount);

    Optional<MemberCoupon> findByUsedOrderId(Long orderId);
}
