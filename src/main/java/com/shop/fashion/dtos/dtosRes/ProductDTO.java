package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private int price;
    private int percent;
    private String image;
    private String createdDate;
    // SubCategoryDTO subCategory;

}
