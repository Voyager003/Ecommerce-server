package com.ecommerce.domain.product.dto;

import com.ecommerce.domain.product.domain.ProductOption;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductOptionResponse {

    private Long id;
    private String name;
    private long additionalPrice;
    private long totalPrice;

    public static ProductOptionResponse from(ProductOption option) {
        return ProductOptionResponse.builder()
                .id(option.getId())
                .name(option.getName())
                .additionalPrice(option.getAdditionalPrice().getAmount())
                .totalPrice(option.getTotalPrice().getAmount())
                .build();
    }
}
