package com.ecommerce.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreateRequest {

    @NotEmpty(message = "주문 상품 목록은 필수입니다")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "배송지 정보는 필수입니다")
    @Valid
    private ShippingInfoRequest shippingInfo;

    private Long couponId;
}
