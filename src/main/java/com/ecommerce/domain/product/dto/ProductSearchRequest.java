package com.ecommerce.domain.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchRequest {

    private String keyword;
    private Long categoryId;
    private int page = 0;
    private int size = 20;
    private String sort = "createdAt";
    private String direction = "desc";
}
