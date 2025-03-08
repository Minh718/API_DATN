package com.shop.fashion.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosRes.DetailOrderDTO;
import com.shop.fashion.dtos.dtosRes.OrderResDTO;
import com.shop.fashion.entities.Order;

@Mapper
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    OrderResDTO toOrderResDTO(Order order);

    DetailOrderDTO toDetailOrderDTO(Order order);

}