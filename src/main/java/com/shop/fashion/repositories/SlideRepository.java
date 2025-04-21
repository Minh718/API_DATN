package com.shop.fashion.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.Slide;

@Repository
public interface SlideRepository extends JpaRepository<Slide, Long> {
    List<Slide> findAllByStatus(boolean status);
}