package com.shop.fashion.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosRes.CategoryDTO;
import com.shop.fashion.dtos.dtosRes.CategoryDetail;
import com.shop.fashion.dtos.dtosRes.CategoryTable;
import com.shop.fashion.entities.Category;

@Mapper
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryDTO toCategoryDTO(Category category);

    CategoryTable toCategoryTable(Category category);

    List<CategoryDTO> toListCategoryDTO(List<Category> category);

    CategoryDetail toCategoryDetail(Category category);
    // CartProductDTO toCartProductDTO(CartProduct cartProduct);
}
