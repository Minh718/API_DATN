package com.shop.fashion.dtos.dtosRes;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColorTable {
    private Long id;
    private String name;
    private String code;
}
