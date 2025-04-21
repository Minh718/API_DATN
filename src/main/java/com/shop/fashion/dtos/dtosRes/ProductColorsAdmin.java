package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductColorsAdmin {
    private Long id;
    private String name;
    private String code;
    private long totalQuantity = 0;
    private long totalSales;

    public ProductColorsAdmin(Long id, String name, String color, long totalSales) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.totalSales = totalSales;
    }

}
