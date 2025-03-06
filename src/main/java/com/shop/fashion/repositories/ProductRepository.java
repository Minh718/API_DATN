package com.shop.fashion.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<com.shop.fashion.entities.Product, Long> {

    Page<Product> findAllByIsDraftOrderByCreatedDateDesc(boolean isDraft,
            Pageable pageable);

}