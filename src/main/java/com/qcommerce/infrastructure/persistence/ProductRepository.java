package com.qcommerce.infrastructure.persistence;

import com.qcommerce.domain.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByShopId(Long shopId);

    @Query("""
        SELECT p FROM Product p
        WHERE p.shopId IN :shopIds
          AND p.isActive = true
          AND (:categoryId IS NULL OR p.categoryId = :categoryId)
          AND (:keyword IS NULL OR 
               LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    List<Product> findProductsByShopsWithFilters(
        @Param("shopIds") List<Long> shopIds,
        @Param("categoryId") Long categoryId,
        @Param("keyword") String keyword
    );

    List<Product> findByShopIdAndIsActiveTrue(Long shopId);
}
