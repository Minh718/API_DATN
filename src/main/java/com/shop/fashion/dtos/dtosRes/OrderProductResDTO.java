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
public class OrderProductResDTO {
    private Long id;
    private long quantity;
    private long price;
    private String image;
    private String name;
    private String size;
    private String color;
}
