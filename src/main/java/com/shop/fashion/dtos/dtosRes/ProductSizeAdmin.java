package com.shop.fashion.dtos.dtosRes;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSizeAdmin {
    private Long id;
    String name;
    long totalQuantity = 0;
    long totalSales;

    public ProductSizeAdmin(Long id, String name, long totalSales) {
        this.id = id;
        this.name = name;
        this.totalSales = totalSales;
    }

}
