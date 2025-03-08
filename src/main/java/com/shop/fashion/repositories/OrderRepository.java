package com.shop.fashion.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.Order;
import com.shop.fashion.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderStatus = :orderStatus ORDER BY o.createdAt DESC")
    Page<Order> findAllByUserIdAndOrderStatusOrderByCreatedAtDesc(String userId, OrderStatus orderStatus,
            Pageable pageable);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderProducts op JOIN FETCH op.productSizeColor psc JOIN FETCH psc.productSize WHERE o.id = :id ")
    Optional<Order> findByIdFetchOrderProductFetchProductSizeColorFetchProductSize(Long id);
}