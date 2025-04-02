package com.shop.fashion.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.RecommendProduct;

import feign.Param;

@Repository
public interface RecommendProductRepository extends JpaRepository<RecommendProduct, Long> {
    @Query("SELECT rp FROM RecommendProduct rp WHERE (rp.p1 = :p1 AND rp.p2 = :p2) OR (rp.p1 = :p2 AND rp.p2 = :p1)")
    Optional<RecommendProduct> findByProductPair(Long p1, Long p2);

    @Query("SELECT rp FROM RecommendProduct rp WHERE rp.p1 = :p OR rp.p2 = :p ORDER BY rp.occurrences DESC")
    List<RecommendProduct> findByProduct(@Param("p") Long p, Pageable pageable);
}
