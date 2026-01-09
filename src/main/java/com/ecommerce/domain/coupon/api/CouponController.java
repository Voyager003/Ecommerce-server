package com.ecommerce.domain.coupon.api;

import com.ecommerce.domain.coupon.application.CouponService;
import com.ecommerce.domain.coupon.domain.Coupon;
import com.ecommerce.domain.coupon.domain.MemberCoupon;
import com.ecommerce.domain.coupon.dto.CouponCreateRequest;
import com.ecommerce.domain.coupon.dto.CouponIssueRequest;
import com.ecommerce.domain.coupon.dto.CouponResponse;
import com.ecommerce.domain.coupon.dto.MemberCouponResponse;
import com.ecommerce.global.common.ApiResponse;
import com.ecommerce.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getIssuableCoupons() {
        List<Coupon> coupons = couponService.getIssuableCoupons();
        List<CouponResponse> responses = coupons.stream()
                .map(CouponResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCoupon(@PathVariable Long couponId) {
        Coupon coupon = couponService.getCoupon(couponId);
        return ResponseEntity.ok(ApiResponse.ok(CouponResponse.from(coupon)));
    }

    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<MemberCouponResponse>> issueCoupon(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CouponIssueRequest request) {
        MemberCoupon memberCoupon = couponService.issueCoupon(
                userDetails.getMemberId(), request.getCouponId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(MemberCouponResponse.from(memberCoupon)));
    }
}
