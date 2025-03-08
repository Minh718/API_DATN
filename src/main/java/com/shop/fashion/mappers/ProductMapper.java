package com.shop.fashion.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosRes.ProductDTO;
import com.shop.fashion.dtos.dtosRes.ProductDetailDTO;
import com.shop.fashion.entities.Product;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "detailProduct", ignore = true)
    @Mapping(target = "productSizes", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subCategory", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Product toProduct(ProductDTO productDTO);

    @Mapping(source = "createdDate", target = "createdDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    ProductDTO toProductDTO(Product product);

    ProductDetailDTO toProductDetailDTO(Product product);

}