package com.ecommerce.domain.coupon.api;

import com.ecommerce.domain.coupon.application.CouponService;
import com.ecommerce.domain.coupon.domain.Coupon;
import com.ecommerce.domain.coupon.dto.CouponCreateRequest;
import com.ecommerce.domain.coupon.dto.CouponResponse;
import com.ecommerce.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponCreateRequest request) {
        Coupon coupon = couponService.createCoupon(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(CouponResponse.from(coupon)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {
        List<Coupon> coupons = couponService.getAvailableCoupons();
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

    @PostMapping("/{couponId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateCoupon(@PathVariable Long couponId) {
        couponService.deactivateCoupon(couponId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{couponId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateCoupon(@PathVariable Long couponId) {
        couponService.activateCoupon(couponId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
