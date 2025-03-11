package com.shop.fashion.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosRes.ProductSizeColorDTO;
import com.shop.fashion.entities.ProductSizeColor;

@Mapper
public interface ProductSizeColorMapper {
    ProductSizeColorMapper INSTANCE = Mappers.getMapper(ProductSizeColorMapper.class);

    @Mapping(target = "quantity", ignore = true)
    ProductSizeColorDTO toProductSizeColorDTO(ProductSizeColor productSizeColor);

}