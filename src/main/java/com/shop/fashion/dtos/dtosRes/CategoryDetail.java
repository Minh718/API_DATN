package com.shop.fashion.dtos.dtosRes;

import java.util.List;
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
public class CategoryDetail {
    private Long id;
    private String name;
    private Set<SubCategoryDTO> subCategories;
    private List<SizeDTO> sizes;

}
