package com.shop.fashion.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosReq.OrderDTO;
import com.shop.fashion.dtos.dtosRes.DetailOrderDTO;
import com.shop.fashion.dtos.dtosRes.OrderProductResDTO;
import com.shop.fashion.dtos.dtosRes.OrderResDTO;
import com.shop.fashion.entities.Order;
import com.shop.fashion.entities.OrderProduct;

@Mapper
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    OrderResDTO toOrderResDTO(Order order);

    DetailOrderDTO toDetailOrderDTO(Order order);

    @Mapping(target = "totalAmount", source = "order.checkoutRes.paymentFee")
    @Mapping(target = "discount", source = "order.checkoutRes.discount")
    @Mapping(target = "code", source = "order.checkoutReq.code")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderProducts", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "shippingCost", ignore = true)
    @Mapping(target = "shippingFee", ignore = true)
    @Mapping(target = "shippingStatus", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "urlPayment", ignore = true)
    @Mapping(target = "user", ignore = true)
    Order toOrder(OrderDTO order);

    OrderProductResDTO toOrderProductResDTO(OrderProduct orderProduct);
}