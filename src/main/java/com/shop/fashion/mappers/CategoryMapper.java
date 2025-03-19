package com.shop.fashion.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosRes.CategoryDTO;
import com.shop.fashion.entities.Category;

@Mapper
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryDTO toCategoryDTO(Category category);

    List<CategoryDTO> toListCategoryDTO(List<Category> category);

    // CartProductDTO toCartProductDTO(CartProduct cartProduct);
}
