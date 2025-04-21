package com.shop.fashion.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.Color;
import com.shop.fashion.entities.ProductSize;
import com.shop.fashion.entities.ProductSizeColor;

@Repository
public interface ProductSizeColorRepository extends JpaRepository<ProductSizeColor, Long> {
    @Query("SELECT psc FROM ProductSizeColor psc JOIN FETCH psc.productSize ps JOIN FETCH ps.product WHERE psc.id = :pscId")
    Optional<ProductSizeColor> findByIdFetchProductSizeAndFetchProduct(long pscId);

    Optional<ProductSizeColor> findByProductSizeAndColor(ProductSize productSize,
            Color color);
}