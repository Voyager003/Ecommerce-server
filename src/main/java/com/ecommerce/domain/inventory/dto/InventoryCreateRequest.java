package com.ecommerce.domain.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InventoryCreateRequest {

    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    private Long productOptionId;

    @NotNull(message = "초기 수량은 필수입니다")
    @Min(value = 0, message = "수량은 0 이상이어야 합니다")
    private Integer quantity;
}
