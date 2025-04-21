package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductTable {
    private Long id;
    private String name;
    private Integer price;
    private String image;
    private Integer percent;
    private Boolean status;
    private Integer totalSales;
    private Long totalRevenue;
    private Integer stockLeft = 0;

    public ProductTable() {

    }

    public ProductTable(Long id, String name, Integer price, String image, Integer percent,
            Boolean status, Long totalSales, Long totalRevenue) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.percent = percent;
        this.status = status;
        this.totalSales = totalSales != null ? totalSales.intValue() : 0;
        this.totalRevenue = totalRevenue;
    }
}
