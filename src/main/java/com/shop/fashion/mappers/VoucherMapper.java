package com.shop.fashion.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosRes.VoucherResDTO;
import com.shop.fashion.entities.Voucher;

@Mapper
public interface VoucherMapper {
    VoucherMapper INSTANCE = Mappers.getMapper(VoucherMapper.class);

    VoucherResDTO toVoucherResDTO(Voucher voucher);

    // UserInfo toUserInfo(User user);
}