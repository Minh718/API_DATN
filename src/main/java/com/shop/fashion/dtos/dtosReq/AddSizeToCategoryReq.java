package com.shop.fashion.dtos.dtosReq;

import lombok.AllArgsConstructor;
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
public class AddSizeToCategoryReq {
    private Long idCategory;
    private String name;
}
