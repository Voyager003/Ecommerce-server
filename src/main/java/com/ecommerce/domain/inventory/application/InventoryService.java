package com.ecommerce.domain.inventory.application;

import com.ecommerce.domain.inventory.dao.InventoryHistoryRepository;
import com.ecommerce.domain.inventory.dao.InventoryRepository;
import com.ecommerce.domain.inventory.domain.Inventory;
import com.ecommerce.domain.inventory.domain.InventoryHistory;
import com.ecommerce.domain.inventory.exception.InventoryException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private static final int MAX_RETRY = 3;

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    public int getAvailableQuantity(Long productId, Long optionId) {
        Inventory inventory = findInventory(productId, optionId);
        return inventory.getAvailableQuantity();
    }

    public boolean hasStock(Long productId, Long optionId, int quantity) {
        Inventory inventory = findInventory(productId, optionId);
        return inventory.hasAvailableStock(quantity);
    }

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = MAX_RETRY,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void deductStock(Long productId, Long optionId, int quantity, Long orderId) {
        Inventory inventory = findInventoryWithLock(productId, optionId);

        int beforeQuantity = inventory.getQuantity();
        inventory.deductStock(quantity);

        InventoryHistory history = InventoryHistory.createDeductHistory(inventory, quantity, orderId);
        inventoryHistoryRepository.save(history);
    }

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = MAX_RETRY,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void restoreStock(Long productId, Long optionId, int quantity, Long orderId) {
        Inventory inventory = findInventoryWithLock(productId, optionId);

        inventory.restore(quantity);

        InventoryHistory history = InventoryHistory.createRestoreHistory(inventory, quantity, orderId);
        inventoryHistoryRepository.save(history);
    }

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = MAX_RETRY,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void addStock(Long productId, Long optionId, int quantity, String reason) {
        Inventory inventory = findInventoryWithLock(productId, optionId);

        inventory.addStock(quantity);

        InventoryHistory history = InventoryHistory.createInboundHistory(inventory, quantity, reason);
        inventoryHistoryRepository.save(history);
    }

    @Transactional
    public void reserve(Long productId, Long optionId, int quantity) {
        Inventory inventory = findInventoryWithLock(productId, optionId);
        inventory.reserve(quantity);
    }

    @Transactional
    public void confirmReservation(Long productId, Long optionId, int quantity, Long orderId) {
        Inventory inventory = findInventoryWithLock(productId, optionId);
        inventory.confirmReservation(quantity);

        InventoryHistory history = InventoryHistory.createDeductHistory(inventory, quantity, orderId);
        inventoryHistoryRepository.save(history);
    }

    @Transactional
    public void cancelReservation(Long productId, Long optionId, int quantity) {
        Inventory inventory = findInventoryWithLock(productId, optionId);
        inventory.cancelReservation(quantity);
    }

    // Admin API methods
    public Inventory getInventory(Long productId, Long optionId) {
        return findInventory(productId, optionId);
    }

    public java.util.List<Inventory> getInventoriesByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Transactional
    public Inventory createInventory(Long productId, Long optionId, int quantity) {
        Inventory inventory = Inventory.builder()
                .productId(productId)
                .productOptionId(optionId)
                .quantity(quantity)
                .build();
        return inventoryRepository.save(inventory);
    }

    public org.springframework.data.domain.Page<InventoryHistory> getInventoryHistory(
            Long inventoryId, org.springframework.data.domain.Pageable pageable) {
        return inventoryHistoryRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId, pageable);
    }

    private Inventory findInventory(Long productId, Long optionId) {
        if (optionId == null) {
            return inventoryRepository.findByProductIdAndProductOptionIdIsNull(productId)
                    .orElseThrow(InventoryException::notFound);
        }
        return inventoryRepository.findByProductIdAndProductOptionId(productId, optionId)
                .orElseThrow(InventoryException::notFound);
    }

    private Inventory findInventoryWithLock(Long productId, Long optionId) {
        if (optionId == null) {
            return inventoryRepository.findByProductIdWithLock(productId)
                    .orElseThrow(InventoryException::notFound);
        }
        return inventoryRepository.findByProductIdAndOptionIdWithLock(productId, optionId)
                .orElseThrow(InventoryException::notFound);
    }
}
