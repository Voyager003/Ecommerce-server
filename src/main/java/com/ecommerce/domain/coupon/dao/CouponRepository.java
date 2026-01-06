package com.ecommerce.domain.coupon.dao;

import com.ecommerce.domain.coupon.domain.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startDate <= :today AND c.endDate >= :today")
    List<Coupon> findAvailableCoupons(@Param("today") LocalDate today);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startDate <= :today AND c.endDate >= :today " +
            "AND (c.totalQuantity IS NULL OR c.issuedQuantity < c.totalQuantity)")
    List<Coupon> findIssuableCoupons(@Param("today") LocalDate today);

    List<Coupon> findByIsActiveTrue();
}
