package com.shop.fashion.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.CartProductSizeColor;

@Repository
public interface CartProductSizeColorRepository extends JpaRepository<CartProductSizeColor, Long> {
    Optional<CartProductSizeColor> findByCartIdAndProductSizeColorId(String cartId,
            long productSizeColorId);

    Optional<CartProductSizeColor> findByIdAndCartId(long id, String cartId);

    @Query("SELECT cpsc FROM CartProductSizeColor cpsc WHERE cpsc.cart.id = :cartId ORDER BY cpsc.updateAt DESC")
    Page<CartProductSizeColor> findAllByCartIdOrderByUpdateAtDesc(String cartId,
            Pageable pageable);

}