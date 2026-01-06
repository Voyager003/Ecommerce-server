package com.ecommerce.domain.order.dto;

import com.ecommerce.domain.order.domain.OrderItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private Long productOptionId;
    private String productName;
    private String optionName;
    private long unitPrice;
    private int quantity;
    private long subtotal;

    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productOptionId(item.getProductOptionId())
                .productName(item.getProductName())
                .optionName(item.getOptionName())
                .unitPrice(item.getUnitPrice().getAmount())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal().getAmount())
                .build();
    }
}
