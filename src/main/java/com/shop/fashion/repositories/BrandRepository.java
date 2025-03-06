package com.shop.fashion.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
}
