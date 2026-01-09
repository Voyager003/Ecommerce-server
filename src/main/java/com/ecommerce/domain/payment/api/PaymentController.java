package com.ecommerce.domain.payment.api;

import com.ecommerce.domain.payment.application.PaymentService;
import com.ecommerce.domain.payment.dto.PaymentRequest;
import com.ecommerce.domain.payment.dto.PaymentResponse;
import com.ecommerce.global.common.ApiResponse;
import com.ecommerce.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(
                userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPayment(
                userDetails.getMemberId(), paymentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(
                userDetails.getMemberId(), orderId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long paymentId) {
        PaymentResponse response = paymentService.cancelPayment(
                userDetails.getMemberId(), paymentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
