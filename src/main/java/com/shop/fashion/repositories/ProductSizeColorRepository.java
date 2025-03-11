package com.shop.fashion.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.ProductSizeColor;

@Repository
public interface ProductSizeColorRepository extends JpaRepository<ProductSizeColor, Long> {
}