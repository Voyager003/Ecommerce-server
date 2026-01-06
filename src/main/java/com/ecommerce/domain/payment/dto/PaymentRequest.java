package com.ecommerce.domain.payment.dto;

import com.ecommerce.domain.payment.domain.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PaymentRequest {

    @NotNull(message = "주문 ID는 필수입니다")
    private Long orderId;

    @NotNull(message = "결제 수단은 필수입니다")
    private PaymentMethod method;

    @NotBlank(message = "멱등키는 필수입니다")
    private String idempotencyKey;

    private String cardNumber;
}
