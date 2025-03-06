package com.shop.fashion.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.SubCategory;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    @Query("SELECT s FROM SubCategory s LEFT JOIN FETCH s.category WHERE s.id = :id")
    Optional<SubCategory> findByIdWithCategory(@Param("id") Long id);

    Optional<SubCategory> findByThump(String thump);
}