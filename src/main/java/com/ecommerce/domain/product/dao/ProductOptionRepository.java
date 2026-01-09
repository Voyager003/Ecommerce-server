package com.ecommerce.domain.product.dao;

import com.ecommerce.domain.product.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    List<ProductOption> findByProductIdAndActiveTrue(Long productId);

    Optional<ProductOption> findByIdAndProductId(Long id, Long productId);

    @Query("SELECT po FROM ProductOption po WHERE po.id IN :ids AND po.product.id = :productId AND po.active = true")
    List<ProductOption> findByIdsAndProductIdAndActiveTrue(@Param("ids") List<Long> ids,
                                                            @Param("productId") Long productId);

    int countByProductId(Long productId);
}
