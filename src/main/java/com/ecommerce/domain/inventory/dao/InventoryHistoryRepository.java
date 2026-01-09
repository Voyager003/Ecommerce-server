package com.ecommerce.domain.inventory.dao;

import com.ecommerce.domain.inventory.domain.InventoryHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

    Page<InventoryHistory> findByInventoryIdOrderByCreatedAtDesc(Long inventoryId, Pageable pageable);

    List<InventoryHistory> findByOrderId(Long orderId);
}
