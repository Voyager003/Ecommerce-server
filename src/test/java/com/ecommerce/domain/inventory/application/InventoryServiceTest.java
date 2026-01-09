package com.ecommerce.domain.inventory.application;

import com.ecommerce.domain.inventory.dao.InventoryHistoryRepository;
import com.ecommerce.domain.inventory.dao.InventoryRepository;
import com.ecommerce.domain.inventory.domain.Inventory;
import com.ecommerce.domain.inventory.domain.InventoryHistory;
import com.ecommerce.domain.inventory.exception.InventoryException;
import com.ecommerce.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Nested
    @DisplayName("재고 가용 수량 조회")
    class GetAvailableQuantityTest {

        @Test
        @DisplayName("상품의 가용 수량을 조회한다")
        void getAvailableQuantity_Success() {
            // given
            Inventory inventory = createInventory(1L, 100);
            given(inventoryRepository.findByProductIdAndProductOptionId(1L, 1L))
                    .willReturn(Optional.of(inventory));

            // when
            int availableQuantity = inventoryService.getAvailableQuantity(1L, 1L);

            // then
            assertThat(availableQuantity).isEqualTo(100);
        }

        @Test
        @DisplayName("옵션이 없는 상품의 가용 수량을 조회한다")
        void getAvailableQuantity_NoOption_Success() {
            // given
            Inventory inventory = createInventory(1L, 50);
            given(inventoryRepository.findByProductIdAndProductOptionIdIsNull(1L))
                    .willReturn(Optional.of(inventory));

            // when
            int availableQuantity = inventoryService.getAvailableQuantity(1L, null);

            // then
            assertThat(availableQuantity).isEqualTo(50);
        }

        @Test
        @DisplayName("재고가 없으면 예외가 발생한다")
        void getAvailableQuantity_NotFound_ThrowsException() {
            // given
            given(inventoryRepository.findByProductIdAndProductOptionId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inventoryService.getAvailableQuantity(999L, 999L))
                    .isInstanceOf(InventoryException.class)
                    .satisfies(e -> {
                        InventoryException ex = (InventoryException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVENTORY_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("재고 보유 여부 확인")
    class HasStockTest {

        @Test
        @DisplayName("요청 수량보다 많은 재고가 있으면 true를 반환한다")
        void hasStock_Enough_ReturnsTrue() {
            // given
            Inventory inventory = createInventory(1L, 100);
            given(inventoryRepository.findByProductIdAndProductOptionId(1L, 1L))
                    .willReturn(Optional.of(inventory));

            // when
            boolean result = inventoryService.hasStock(1L, 1L, 50);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("요청 수량보다 적은 재고가 있으면 false를 반환한다")
        void hasStock_NotEnough_ReturnsFalse() {
            // given
            Inventory inventory = createInventory(1L, 30);
            given(inventoryRepository.findByProductIdAndProductOptionId(1L, 1L))
                    .willReturn(Optional.of(inventory));

            // when
            boolean result = inventoryService.hasStock(1L, 1L, 50);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("재고 차감")
    class DeductStockTest {

        @Test
        @DisplayName("재고를 차감하고 이력을 기록한다")
        void deductStock_Success() {
            // given
            Inventory inventory = createInventory(1L, 100);
            given(inventoryRepository.findByProductIdAndOptionIdWithLock(1L, 1L))
                    .willReturn(Optional.of(inventory));

            // when
            inventoryService.deductStock(1L, 1L, 30, 1001L);

            // then
            assertThat(inventory.getQuantity()).isEqualTo(70);
            verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
        }
    }

    @Nested
    @DisplayName("재고 복원")
    class RestoreStockTest {

        @Test
        @DisplayName("재고를 복원하고 이력을 기록한다")
        void restoreStock_Success() {
            // given
            Inventory inventory = createInventory(1L, 70);
            given(inventoryRepository.findByProductIdAndOptionIdWithLock(1L, 1L))
                    .willReturn(Optional.of(inventory));

            // when
            inventoryService.restoreStock(1L, 1L, 30, 1001L);

            // then
            assertThat(inventory.getQuantity()).isEqualTo(100);
            verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
        }
    }

    @Nested
    @DisplayName("재고 입고")
    class AddStockTest {

        @Test
        @DisplayName("재고를 입고하고 이력을 기록한다")
        void addStock_Success() {
            // given
            Inventory inventory = createInventory(1L, 100);
            given(inventoryRepository.findByProductIdAndOptionIdWithLock(1L, 1L))
                    .willReturn(Optional.of(inventory));

            // when
            inventoryService.addStock(1L, 1L, 50, "정기 입고");

            // then
            assertThat(inventory.getQuantity()).isEqualTo(150);
            verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
        }
    }

    @Nested
    @DisplayName("재고 예약")
    class ReserveTest {

        @Test
        @DisplayName("재고를 예약한다")
        void reserve_Success() {
            // given
            Inventory inventory = createInventory(1L, 100);
            given(inventoryRepository.findByProductIdAndOptionIdWithLock(1L, 1L))
                    .willReturn(Optional.of(inventory));

            // when
            inventoryService.reserve(1L, 1L, 30);

            // then
            assertThat(inventory.getReservedQuantity()).isEqualTo(30);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(70);
        }
    }

    @Nested
    @DisplayName("예약 확정")
    class ConfirmReservationTest {

        @Test
        @DisplayName("예약을 확정하고 이력을 기록한다")
        void confirmReservation_Success() {
            // given
            Inventory inventory = createInventory(1L, 100);
            inventory.reserve(30);
            given(inventoryRepository.findByProductIdAndOptionIdWithLock(1L, 1L))
                    .willReturn(Optional.of(inventory));

            // when
            inventoryService.confirmReservation(1L, 1L, 30, 1001L);

            // then
            assertThat(inventory.getQuantity()).isEqualTo(70);
            assertThat(inventory.getReservedQuantity()).isEqualTo(0);
            verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
        }
    }

    @Nested
    @DisplayName("예약 취소")
    class CancelReservationTest {

        @Test
        @DisplayName("예약을 취소하여 가용 재고를 복원한다")
        void cancelReservation_Success() {
            // given
            Inventory inventory = createInventory(1L, 100);
            inventory.reserve(30);
            given(inventoryRepository.findByProductIdAndOptionIdWithLock(1L, 1L))
                    .willReturn(Optional.of(inventory));

            // when
            inventoryService.cancelReservation(1L, 1L, 30);

            // then
            assertThat(inventory.getReservedQuantity()).isEqualTo(0);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("재고 생성")
    class CreateInventoryTest {

        @Test
        @DisplayName("새 재고를 생성한다")
        void createInventory_Success() {
            // given
            Inventory savedInventory = createInventory(1L, 100);
            given(inventoryRepository.save(any(Inventory.class))).willReturn(savedInventory);

            // when
            Inventory result = inventoryService.createInventory(1L, 1L, 100);

            // then
            assertThat(result.getProductId()).isEqualTo(1L);
            assertThat(result.getQuantity()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("상품별 재고 목록 조회")
    class GetInventoriesByProductIdTest {

        @Test
        @DisplayName("상품의 모든 재고를 조회한다")
        void getInventoriesByProductId_Success() {
            // given
            List<Inventory> inventories = List.of(
                    createInventory(1L, 100),
                    createInventory(2L, 50)
            );
            given(inventoryRepository.findByProductId(1L)).willReturn(inventories);

            // when
            List<Inventory> result = inventoryService.getInventoriesByProductId(1L);

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("재고 변경 이력 조회")
    class GetInventoryHistoryTest {

        @Test
        @DisplayName("재고 변경 이력을 페이징하여 조회한다")
        void getInventoryHistory_Success() {
            // given
            Page<InventoryHistory> historyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            given(inventoryHistoryRepository.findByInventoryIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 20)))
                    .willReturn(historyPage);

            // when
            Page<InventoryHistory> result = inventoryService.getInventoryHistory(1L, PageRequest.of(0, 20));

            // then
            assertThat(result).isNotNull();
        }
    }

    private Inventory createInventory(Long id, int quantity) {
        Inventory inventory = Inventory.builder()
                .productId(1L)
                .productOptionId(1L)
                .quantity(quantity)
                .build();
        ReflectionTestUtils.setField(inventory, "id", id);
        return inventory;
    }
}
