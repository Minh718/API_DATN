package com.shop.fashion.dtos.dtosRes;

import java.util.Set;

import com.shop.fashion.entities.Brand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDTO {
    private Long id;
    private String name;
    private int price;
    private int percent;
    private String image;
    private Brand brand;
    private int rating;
    private int reviews;
    private SubCategoryDTO subCategory;
    private Set<ProductSizeQuantity> productSizes;
    private DetailProductDTO detailProduct;
}
