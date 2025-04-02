package com.shop.fashion.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.Payment;

@Repository
public interface PaymentRepository
                extends JpaRepository<Payment, Long> {
        Optional<Payment> findByTransactionID(String id);
}