package com.ecommerce.domain.order.dto;

import com.ecommerce.domain.order.domain.Order;
import com.ecommerce.domain.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private long totalAmount;
    private long discountAmount;
    private long deliveryFee;
    private long finalAmount;
    private List<OrderItemResponse> items;
    private ShippingInfoResponse shippingInfo;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount().getAmount())
                .discountAmount(order.getDiscountAmount().getAmount())
                .deliveryFee(order.getDeliveryFee().getAmount())
                .finalAmount(order.getFinalAmount().getAmount())
                .items(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList())
                .shippingInfo(ShippingInfoResponse.from(order.getShippingInfo()))
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .build();
    }
}
