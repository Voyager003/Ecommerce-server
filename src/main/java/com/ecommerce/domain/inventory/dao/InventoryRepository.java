package com.ecommerce.domain.inventory.dao;

import com.ecommerce.domain.inventory.domain.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndProductOptionIdIsNull(Long productId);

    Optional<Inventory> findByProductIdAndProductOptionId(Long productId, Long productOptionId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.productOptionId IS NULL")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") Long productId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.productOptionId = :optionId")
    Optional<Inventory> findByProductIdAndOptionIdWithLock(@Param("productId") Long productId,
                                                            @Param("optionId") Long optionId);

    List<Inventory> findByProductId(Long productId);
}
