package com.qcommerce.infrastructure.persistence;

import com.qcommerce.domain.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long productId);

    List<ProductVariant> findByProductIdIn(List<Long> productIds);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.productId IN :productIds AND pv.isActive = true")
    List<ProductVariant> findActiveVariantsByProductIds(List<Long> productIds);
}
