package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSizeDTO {
    private Long id;
    private int quantity;
    private SizeDTO size;
    private ProductDTO product;
}
