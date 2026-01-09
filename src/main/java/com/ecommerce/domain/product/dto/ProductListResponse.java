package com.ecommerce.domain.product.dto;

import com.ecommerce.domain.product.domain.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductListResponse {

    private Long id;
    private String name;
    private long basePrice;
    private long sellingPrice;
    private long discountRate;
    private String categoryName;
    private boolean hasOption;
    private String thumbnailUrl;

    public static ProductListResponse from(Product product) {
        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .basePrice(product.getBasePrice().getAmount())
                .sellingPrice(product.getSellingPrice().getAmount())
                .discountRate(product.getDiscountRate())
                .categoryName(product.getCategory().getName())
                .hasOption(product.isHasOption())
                .thumbnailUrl(product.getThumbnailUrl())
                .build();
    }
}
