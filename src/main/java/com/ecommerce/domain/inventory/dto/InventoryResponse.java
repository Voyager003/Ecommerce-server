package com.ecommerce.domain.inventory.dto;

import com.ecommerce.domain.inventory.domain.Inventory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InventoryResponse {

    private Long id;
    private Long productId;
    private Long productOptionId;
    private int quantity;
    private int reservedQuantity;
    private int availableQuantity;
    private boolean outOfStock;
    private LocalDateTime updatedAt;

    public static InventoryResponse from(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .productOptionId(inventory.getProductOptionId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .outOfStock(inventory.isOutOfStock())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
