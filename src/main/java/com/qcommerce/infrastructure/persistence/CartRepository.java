package com.qcommerce.infrastructure.persistence;

import com.qcommerce.domain.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Cart Repository - Data access for shopping carts
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find active cart for user
     */
    Optional<Cart> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find active cart for user and shop
     */
    Optional<Cart> findByUserIdAndShopIdAndIsActiveTrue(Long userId, Long shopId);

    /**
     * Find cart with items eagerly loaded
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.id = :cartId")
    Optional<Cart> findByIdWithItems(@Param("cartId") Long cartId);

    /**
     * Find active cart for user with items
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :userId AND c.isActive = true")
    Optional<Cart> findActiveCartByUserWithItems(@Param("userId") Long userId);
}
