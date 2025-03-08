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
public class DetailProductDTO {
    private String description;
    private String material;
    private String origin;
    private String warranty;
    private String madeIn;
    private String model;
    private List<String> images;
}
