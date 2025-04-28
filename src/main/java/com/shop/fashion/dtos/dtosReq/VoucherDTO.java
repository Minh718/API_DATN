package com.shop.fashion.dtos.dtosReq;

import java.time.LocalDate;

import com.shop.fashion.enums.TypeVoucher;
import com.shop.fashion.enums.VoucherTargetType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Voucher
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherDTO {
    private String code;
    private TypeVoucher type;
    private long discount;
    private long minPrice;
    private long maxDiscount;
    private boolean isActive = true;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private VoucherTargetType targetUserType;
}