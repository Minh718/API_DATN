package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartProductSizeColorDTO {
    private Long id;
    private int quantity;
    private String updateAt;
    ProductSizeColorDTO productSizeColor;
}
