package com.shop.fashion.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosRes.CartProductSizeColorDTO;
import com.shop.fashion.entities.CartProductSizeColor;

@Mapper
public interface CartProductSizeColorMapper {

    CartProductSizeColorMapper INSTANCE = Mappers.getMapper(CartProductSizeColorMapper.class);

    // }
    @Mapping(target = "productSizeColor", ignore = true)
    CartProductSizeColorDTO toCartProductSizeColorDTO(CartProductSizeColor cartProductSizeColor);

    List<CartProductSizeColorDTO> toCartProductSizeColorDTOs(
            List<CartProductSizeColor> cartProductSizeColors);

}
