package com.shop.fashion.entities;

import java.time.LocalDateTime;

import com.shop.fashion.enums.PaymentMethod;
import com.shop.fashion.enums.PaymentStatus;
import com.shop.fashion.utils.DateTimeUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private long amount;

    private long discount = 0L;
    private long fee = 0L;

    @Column(unique = true, nullable = false)
    private String transactionID; // Ensure uniqueness to prevent duplicate transactions

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    // @OneToOne
    // @MapsId // This tells JPA to use the Order's ID
    // @JoinColumn(name = "id")
    // private Order order;
    private Long orderId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = DateTimeUtil.getCurrentVietnamTime();
    }
}