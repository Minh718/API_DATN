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
public class ProductDetailAdmin {
    private Long id;
    private String name;
    private int price;
    private int percent;
    private String subCategory;
    private String brand;
    // private String image;
    // private LocalDateTime createdDate;
    // private SubCategoryDTO subCategory;
    private DetailProductDTO detailProduct;
    private List<ProductSizeAdmin> sizes;
}
