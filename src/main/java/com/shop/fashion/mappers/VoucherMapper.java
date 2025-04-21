package com.shop.fashion.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosReq.VoucherDTO;
import com.shop.fashion.dtos.dtosRes.VoucherResDTO;
import com.shop.fashion.entities.Voucher;

@Mapper
public interface VoucherMapper {
    VoucherMapper INSTANCE = Mappers.getMapper(VoucherMapper.class);

    VoucherResDTO toVoucherResDTO(Voucher voucher);

    Voucher toVoucher(VoucherDTO voucherDTO);

    Voucher toVoucher(VoucherResDTO voucherDTO);

    List<VoucherResDTO> toVoucherResDTOs(List<Voucher> vouchers);
    // UserInfo toUserInfo(User user);
}