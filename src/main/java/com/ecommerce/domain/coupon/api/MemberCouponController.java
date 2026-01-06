package com.ecommerce.domain.coupon.api;

import com.ecommerce.domain.coupon.application.CouponCalculator;
import com.ecommerce.domain.coupon.application.CouponService;
import com.ecommerce.domain.coupon.domain.MemberCoupon;
import com.ecommerce.domain.coupon.dto.ApplicableCouponResponse;
import com.ecommerce.domain.coupon.dto.MemberCouponResponse;
import com.ecommerce.domain.model.Money;
import com.ecommerce.global.common.ApiResponse;
import com.ecommerce.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/coupons")
@RequiredArgsConstructor
public class MemberCouponController {

    private final CouponService couponService;
    private final CouponCalculator couponCalculator;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberCouponResponse>>> getMyCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MemberCoupon> memberCoupons = couponService.getMemberCoupons(userDetails.getMemberId());
        List<MemberCouponResponse> responses = memberCoupons.stream()
                .map(MemberCouponResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<MemberCouponResponse>>> getMyAvailableCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MemberCoupon> memberCoupons = couponService.getAvailableMemberCoupons(userDetails.getMemberId());
        List<MemberCouponResponse> responses = memberCoupons.stream()
                .map(MemberCouponResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/applicable")
    public ResponseEntity<ApiResponse<List<ApplicableCouponResponse>>> getApplicableCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long orderAmount) {
        List<MemberCoupon> memberCoupons = couponService.getApplicableCoupons(
                userDetails.getMemberId(), orderAmount);

        Money amount = Money.of(orderAmount);
        List<ApplicableCouponResponse> responses = memberCoupons.stream()
                .filter(mc -> couponCalculator.isApplicable(mc, amount))
                .map(mc -> ApplicableCouponResponse.from(mc, amount))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{memberCouponId}")
    public ResponseEntity<ApiResponse<MemberCouponResponse>> getMyCoupon(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long memberCouponId) {
        MemberCoupon memberCoupon = couponService.getMemberCoupon(memberCouponId);
        return ResponseEntity.ok(ApiResponse.ok(MemberCouponResponse.from(memberCoupon)));
    }
}
