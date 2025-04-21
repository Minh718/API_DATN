package com.shop.fashion.dtos.dtosRes;

import java.time.LocalDate;

import com.shop.fashion.enums.TypeVoucher;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoucherResDTO {
    private Long id;
    private String code;
    @Enumerated(EnumType.STRING)
    private TypeVoucher type;
    private long discount;
    private long minPrice;
    private long maxDiscount;
    private boolean isActive;
    private String description;
    private LocalDate startDate;
    private boolean forNewUser;
    private LocalDate endDate;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}