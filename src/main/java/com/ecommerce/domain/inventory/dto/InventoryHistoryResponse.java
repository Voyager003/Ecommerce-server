package com.ecommerce.domain.inventory.dto;

import com.ecommerce.domain.inventory.domain.InventoryChangeType;
import com.ecommerce.domain.inventory.domain.InventoryHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InventoryHistoryResponse {

    private Long id;
    private Long inventoryId;
    private InventoryChangeType changeType;
    private int changeQuantity;
    private int beforeQuantity;
    private int afterQuantity;
    private Long orderId;
    private String reason;
    private LocalDateTime createdAt;

    public static InventoryHistoryResponse from(InventoryHistory history) {
        return InventoryHistoryResponse.builder()
                .id(history.getId())
                .inventoryId(history.getInventoryId())
                .changeType(history.getChangeType())
                .changeQuantity(history.getChangeQuantity())
                .beforeQuantity(history.getBeforeQuantity())
                .afterQuantity(history.getAfterQuantity())
                .orderId(history.getOrderId())
                .reason(history.getReason())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
