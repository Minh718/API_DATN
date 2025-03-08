package com.shop.fashion.dtos.dtosRes;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSizeQuantity {
    private Long id;
    private int quantity;
    private SizeDTO size;
    private List<ColorQuantityRes> productSizeColors;
}
