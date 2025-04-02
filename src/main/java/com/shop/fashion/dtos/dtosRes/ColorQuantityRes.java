package com.shop.fashion.dtos.dtosRes;

import lombok.Data;

@Data
public class ColorQuantityRes {

    private Long id;
    private int quantity;

    private ColorRes color;
}
