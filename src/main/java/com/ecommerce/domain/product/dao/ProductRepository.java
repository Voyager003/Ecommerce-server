package com.ecommerce.domain.product.dao;

import com.ecommerce.domain.product.domain.Product;
import com.ecommerce.domain.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    @Query("SELECT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.options WHERE p.id = :id")
    Optional<Product> findByIdWithCategoryAndOptions(@Param("id") Long id);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = :status AND " +
           "(p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchByKeyword(@Param("keyword") String keyword,
                                   @Param("status") ProductStatus status,
                                   Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds AND p.status = :status")
    Page<Product> findByCategoryIdsAndStatus(@Param("categoryIds") List<Long> categoryIds,
                                              @Param("status") ProductStatus status,
                                              Pageable pageable);

    List<Product> findTop10ByStatusOrderByCreatedAtDesc(ProductStatus status);
}
