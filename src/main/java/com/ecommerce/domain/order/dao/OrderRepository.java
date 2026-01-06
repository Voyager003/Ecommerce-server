package com.ecommerce.domain.order.dao;

import com.ecommerce.domain.order.domain.Order;
import com.ecommerce.domain.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

    Page<Order> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    List<Order> findByMemberIdAndStatusIn(Long memberId, List<OrderStatus> statuses);

    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.memberId = :memberId AND o.status IN :statuses")
    boolean existsPendingOrder(@Param("memberId") Long memberId, @Param("statuses") List<OrderStatus> statuses);
}
