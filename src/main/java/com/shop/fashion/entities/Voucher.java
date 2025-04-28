package com.shop.fashion.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.shop.fashion.enums.TypeVoucher;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Voucher
 */
@Entity
@Getter
@Setter
@Builder
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
    @Builder.Default
    private boolean isActive = true;
    private String description;
    private LocalDate startDate;
    @Builder.Default
    private boolean forNewUser = false;
    private LocalDate endDate;
    @Builder.Default
    private LocalDate createdAt = LocalDate.now();
    @Builder.Default
    private LocalDate updatedAt = LocalDate.now();
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserVoucher> userVouchers = new ArrayList<>();
}