package com.ecommerce.domain.product.dto;

import com.ecommerce.domain.product.domain.Product;
import com.ecommerce.domain.product.domain.ProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private long basePrice;
    private long sellingPrice;
    private long discountRate;
    private Long categoryId;
    private String categoryName;
    private ProductStatus status;
    private boolean hasOption;
    private String thumbnailUrl;
    private List<ProductOptionResponse> options;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice().getAmount())
                .sellingPrice(product.getSellingPrice().getAmount())
                .discountRate(product.getDiscountRate())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .status(product.getStatus())
                .hasOption(product.isHasOption())
                .thumbnailUrl(product.getThumbnailUrl())
                .createdAt(product.getCreatedAt())
                .build();
    }

    public static ProductResponse fromWithOptions(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice().getAmount())
                .sellingPrice(product.getSellingPrice().getAmount())
                .discountRate(product.getDiscountRate())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .status(product.getStatus())
                .hasOption(product.isHasOption())
                .thumbnailUrl(product.getThumbnailUrl())
                .options(product.getOptions().stream()
                        .filter(opt -> opt.isActive())
                        .map(ProductOptionResponse::from)
                        .toList())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
