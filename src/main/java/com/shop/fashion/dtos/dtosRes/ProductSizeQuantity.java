package com.shop.fashion.dtos.dtosRes;

import java.util.List;

import lombok.Data;

@Data
public class ProductSizeQuantity {
    private Long id;
    private int quantity;
    private SizeDTO size;
    private List<ColorQuantityRes> productSizeColors;
}
