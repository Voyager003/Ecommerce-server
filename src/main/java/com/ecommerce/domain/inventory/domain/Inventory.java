package com.ecommerce.domain.inventory.domain;

import com.ecommerce.domain.inventory.exception.InventoryException;
import com.ecommerce.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "product_option_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    private Long productOptionId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int reservedQuantity = 0;

    @Version
    private Long version;

    @Builder
    public Inventory(Long productId, Long productOptionId, int quantity) {
        this.productId = productId;
        this.productOptionId = productOptionId;
        this.quantity = quantity;
        this.reservedQuantity = 0;
    }

    public int getAvailableQuantity() {
        return this.quantity - this.reservedQuantity;
    }

    public boolean hasAvailableStock(int requestQuantity) {
        return getAvailableQuantity() >= requestQuantity;
    }

    public void reserve(int requestQuantity) {
        if (!hasAvailableStock(requestQuantity)) {
            throw InventoryException.insufficientStock();
        }
        this.reservedQuantity += requestQuantity;
    }

    public void confirmReservation(int confirmedQuantity) {
        if (this.reservedQuantity < confirmedQuantity) {
            throw new IllegalStateException("확정 수량이 예약 수량보다 많습니다.");
        }
        this.quantity -= confirmedQuantity;
        this.reservedQuantity -= confirmedQuantity;
    }

    public void cancelReservation(int cancelQuantity) {
        if (this.reservedQuantity < cancelQuantity) {
            throw new IllegalStateException("취소 수량이 예약 수량보다 많습니다.");
        }
        this.reservedQuantity -= cancelQuantity;
    }

    public void restore(int restoreQuantity) {
        this.quantity += restoreQuantity;
    }

    public void addStock(int addQuantity) {
        if (addQuantity < 0) {
            throw new IllegalArgumentException("추가 수량은 0 이상이어야 합니다.");
        }
        this.quantity += addQuantity;
    }

    public void deductStock(int deductQuantity) {
        if (deductQuantity < 0) {
            throw new IllegalArgumentException("차감 수량은 0 이상이어야 합니다.");
        }
        if (this.quantity < deductQuantity) {
            throw InventoryException.insufficientStock();
        }
        this.quantity -= deductQuantity;
    }

    public boolean isOutOfStock() {
        return getAvailableQuantity() <= 0;
    }
}
