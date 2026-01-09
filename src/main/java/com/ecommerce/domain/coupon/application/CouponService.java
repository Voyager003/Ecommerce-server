package com.ecommerce.domain.coupon.application;

import com.ecommerce.domain.coupon.dao.CouponRepository;
import com.ecommerce.domain.coupon.dao.MemberCouponRepository;
import com.ecommerce.domain.coupon.domain.Coupon;
import com.ecommerce.domain.coupon.domain.CouponStatus;
import com.ecommerce.domain.coupon.domain.MemberCoupon;
import com.ecommerce.domain.coupon.exception.CouponException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;

    public List<Coupon> getIssuableCoupons() {
        return couponRepository.findIssuableCoupons(LocalDate.now());
    }

    public List<Coupon> getAvailableCoupons() {
        return couponRepository.findAvailableCoupons(LocalDate.now());
    }

    public List<MemberCoupon> getMemberCoupons(Long memberId) {
        return memberCouponRepository.findByMemberIdWithCoupon(memberId);
    }

    public List<MemberCoupon> getAvailableMemberCoupons(Long memberId) {
        return memberCouponRepository.findByMemberIdAndStatusWithCoupon(memberId, CouponStatus.AVAILABLE);
    }

    public List<MemberCoupon> getApplicableCoupons(Long memberId, Long orderAmount) {
        return memberCouponRepository.findApplicableCoupons(memberId, orderAmount);
    }

    @Transactional
    public MemberCoupon issueCoupon(Long memberId, Long couponId) {
        if (memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)) {
            throw CouponException.alreadyIssued();
        }

        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(CouponException::notFound);

        if (!coupon.isAvailable()) {
            throw CouponException.notAvailable();
        }

        MemberCoupon memberCoupon = MemberCoupon.issue(memberId, coupon);
        return memberCouponRepository.save(memberCoupon);
    }

    @Transactional
    public void useCoupon(Long memberCouponId, Long orderId) {
        MemberCoupon memberCoupon = memberCouponRepository.findById(memberCouponId)
                .orElseThrow(CouponException::notFound);
        memberCoupon.use(orderId);
    }

    @Transactional
    public void useCouponByMemberAndCoupon(Long memberId, Long couponId, Long orderId) {
        MemberCoupon memberCoupon = memberCouponRepository.findByMemberIdAndCouponId(memberId, couponId)
                .orElseThrow(CouponException::notFound);
        memberCoupon.use(orderId);
    }

    @Transactional
    public void restoreCoupon(Long orderId) {
        memberCouponRepository.findByUsedOrderId(orderId)
                .ifPresent(MemberCoupon::restore);
    }

    @Transactional
    public void restoreCouponById(Long memberCouponId) {
        MemberCoupon memberCoupon = memberCouponRepository.findById(memberCouponId)
                .orElseThrow(CouponException::notFound);
        memberCoupon.restore();
        memberCoupon.getCoupon().cancelIssue();
    }

    public Coupon getCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(CouponException::notFound);
    }

    public MemberCoupon getMemberCoupon(Long memberCouponId) {
        return memberCouponRepository.findById(memberCouponId)
                .orElseThrow(CouponException::notFound);
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Transactional
    public void deactivateCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(CouponException::notFound);
        coupon.deactivate();
    }

    @Transactional
    public void activateCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(CouponException::notFound);
        coupon.activate();
    }

    @Transactional
    public void issueWelcomeCoupon(Long memberId, Long couponId) {
        if (memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)) {
            return;
        }

        couponRepository.findById(couponId).ifPresent(coupon -> {
            if (coupon.isAvailable()) {
                MemberCoupon memberCoupon = MemberCoupon.issue(memberId, coupon);
                memberCouponRepository.save(memberCoupon);
            }
        });
    }
}
