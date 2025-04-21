package com.shop.fashion.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.ProductSize;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {
}