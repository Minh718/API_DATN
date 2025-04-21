package com.shop.fashion.dtos.dtosRes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SizeDTO {
    private Long id;
    private String name;
}
