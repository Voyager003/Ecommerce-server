package com.ecommerce.domain.inventory.domain;

import com.ecommerce.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inventoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryChangeType changeType;

    @Column(nullable = false)
    private int changeQuantity;

    @Column(nullable = false)
    private int beforeQuantity;

    @Column(nullable = false)
    private int afterQuantity;

    private Long orderId;

    private String reason;

    @Builder
    public InventoryHistory(Long inventoryId, InventoryChangeType changeType, int changeQuantity,
                           int beforeQuantity, int afterQuantity, Long orderId, String reason) {
        this.inventoryId = inventoryId;
        this.changeType = changeType;
        this.changeQuantity = changeQuantity;
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
        this.orderId = orderId;
        this.reason = reason;
    }

    public static InventoryHistory createDeductHistory(Inventory inventory, int quantity, Long orderId) {
        return InventoryHistory.builder()
                .inventoryId(inventory.getId())
                .changeType(InventoryChangeType.DEDUCT)
                .changeQuantity(quantity)
                .beforeQuantity(inventory.getQuantity() + quantity)
                .afterQuantity(inventory.getQuantity())
                .orderId(orderId)
                .reason("주문으로 인한 재고 차감")
                .build();
    }

    public static InventoryHistory createRestoreHistory(Inventory inventory, int quantity, Long orderId) {
        return InventoryHistory.builder()
                .inventoryId(inventory.getId())
                .changeType(InventoryChangeType.RESTORE)
                .changeQuantity(quantity)
                .beforeQuantity(inventory.getQuantity() - quantity)
                .afterQuantity(inventory.getQuantity())
                .orderId(orderId)
                .reason("주문 취소로 인한 재고 복원")
                .build();
    }

    public static InventoryHistory createInboundHistory(Inventory inventory, int quantity, String reason) {
        return InventoryHistory.builder()
                .inventoryId(inventory.getId())
                .changeType(InventoryChangeType.INBOUND)
                .changeQuantity(quantity)
                .beforeQuantity(inventory.getQuantity() - quantity)
                .afterQuantity(inventory.getQuantity())
                .reason(reason != null ? reason : "재고 입고")
                .build();
    }
}
