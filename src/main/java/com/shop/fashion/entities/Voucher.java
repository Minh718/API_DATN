package com.shop.fashion.entities;

import java.io.Serializable;
import java.time.LocalDate;

import com.shop.fashion.enums.TypeVoucher;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Voucher
 */
@Entity
@Getter
@Setter
public class Voucher implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @Enumerated(EnumType.STRING)
    private TypeVoucher type;
    private long discount;
    private long minPrice;
    private long maxDiscount;
    private boolean isActive = true;
    private String description;
    private LocalDate startDate;
    private boolean forNewUser = false;
    private LocalDate endDate;
    private LocalDate createdAt = LocalDate.now();
    private LocalDate updatedAt = LocalDate.now();
}