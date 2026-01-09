package com.ecommerce.domain.product.dao;

import com.ecommerce.domain.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNullAndActiveTrueOrderBySortOrderAsc();

    List<Category> findByParentIdAndActiveTrueOrderBySortOrderAsc(Long parentId);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    Optional<Category> findByIdWithChildren(@Param("id") Long id);

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.depth, c.sortOrder")
    List<Category> findAllActiveOrderByDepthAndSortOrder();

    boolean existsByName(String name);
}
