package com.ecommerce.domain.product.dto;

import com.ecommerce.domain.product.domain.Category;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private Long parentId;
    private int depth;
    private int sortOrder;
    private List<CategoryResponse> children;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .depth(category.getDepth())
                .sortOrder(category.getSortOrder())
                .build();
    }

    public static CategoryResponse fromWithChildren(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .depth(category.getDepth())
                .sortOrder(category.getSortOrder())
                .children(category.getChildren().stream()
                        .filter(Category::isActive)
                        .map(CategoryResponse::fromWithChildren)
                        .toList())
                .build();
    }
}
