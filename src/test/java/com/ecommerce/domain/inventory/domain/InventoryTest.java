package com.ecommerce.domain.inventory.domain;

import com.ecommerce.domain.inventory.exception.InventoryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryTest {

    @Nested
    @DisplayName("재고 가용 수량")
    class AvailableQuantityTest {

        @Test
        @DisplayName("가용 수량 = 전체 수량 - 예약 수량")
        void getAvailableQuantity_SubtractsReserved() {
            // given
            Inventory inventory = createInventory(100);
            inventory.reserve(30);

            // when
            int available = inventory.getAvailableQuantity();

            // then
            assertThat(available).isEqualTo(70);
        }
    }

    @Nested
    @DisplayName("재고 예약")
    class ReserveTest {

        @Test
        @DisplayName("가용 수량 내에서 예약한다")
        void reserve_WithinAvailable_Success() {
            // given
            Inventory inventory = createInventory(100);

            // when
            inventory.reserve(30);

            // then
            assertThat(inventory.getReservedQuantity()).isEqualTo(30);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(70);
        }

        @Test
        @DisplayName("가용 수량 초과 시 예외가 발생한다")
        void reserve_ExceedsAvailable_ThrowsException() {
            // given
            Inventory inventory = createInventory(50);

            // when & then
            assertThatThrownBy(() -> inventory.reserve(60))
                    .isInstanceOf(InventoryException.class);
        }
    }

    @Nested
    @DisplayName("예약 확정")
    class ConfirmReservationTest {

        @Test
        @DisplayName("예약된 수량을 확정하면 실제 재고가 차감된다")
        void confirmReservation_DeductsQuantity() {
            // given
            Inventory inventory = createInventory(100);
            inventory.reserve(30);

            // when
            inventory.confirmReservation(30);

            // then
            assertThat(inventory.getQuantity()).isEqualTo(70);
            assertThat(inventory.getReservedQuantity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("예약 취소")
    class CancelReservationTest {

        @Test
        @DisplayName("예약을 취소하면 가용 수량이 복원된다")
        void cancelReservation_RestoresAvailable() {
            // given
            Inventory inventory = createInventory(100);
            inventory.reserve(30);

            // when
            inventory.cancelReservation(30);

            // then
            assertThat(inventory.getReservedQuantity()).isEqualTo(0);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("재고 차감")
    class DeductStockTest {

        @Test
        @DisplayName("재고를 차감한다")
        void deductStock_Success() {
            // given
            Inventory inventory = createInventory(100);

            // when
            inventory.deductStock(30);

            // then
            assertThat(inventory.getQuantity()).isEqualTo(70);
        }

        @Test
        @DisplayName("재고 부족 시 예외가 발생한다")
        void deductStock_InsufficientStock_ThrowsException() {
            // given
            Inventory inventory = createInventory(50);

            // when & then
            assertThatThrownBy(() -> inventory.deductStock(60))
                    .isInstanceOf(InventoryException.class);
        }
    }

    @Nested
    @DisplayName("재고 복원")
    class RestoreTest {

        @Test
        @DisplayName("재고를 복원한다")
        void restore_AddsQuantity() {
            // given
            Inventory inventory = createInventory(50);

            // when
            inventory.restore(30);

            // then
            assertThat(inventory.getQuantity()).isEqualTo(80);
        }
    }

    @Nested
    @DisplayName("재고 입고")
    class AddStockTest {

        @Test
        @DisplayName("재고를 입고한다")
        void addStock_Success() {
            // given
            Inventory inventory = createInventory(100);

            // when
            inventory.addStock(50);

            // then
            assertThat(inventory.getQuantity()).isEqualTo(150);
        }

        @Test
        @DisplayName("음수 입고 시 예외가 발생한다")
        void addStock_NegativeAmount_ThrowsException() {
            // given
            Inventory inventory = createInventory(100);

            // when & then
            assertThatThrownBy(() -> inventory.addStock(-10))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private Inventory createInventory(int quantity) {
        return Inventory.builder()
                .productId(1L)
                .quantity(quantity)
                .build();
    }
}
