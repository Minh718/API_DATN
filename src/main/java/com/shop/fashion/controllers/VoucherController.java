package com.shop.fashion.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosRes.ApiMetaRes;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.MetadataDTO;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.services.VoucherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vouchers")
public class VoucherController {
        private final VoucherService voucherService;

        @GetMapping("/user/all")
        public ApiRes<List<Voucher>> getAllVouchers() {
                return ApiRes.<List<Voucher>>builder().result(voucherService.getAllVouchers())
                                .message("get vouchers susccess")
                                .build();
        }

        @GetMapping("/user/get")
        public ApiMetaRes<List<Voucher>> getVouchers(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
                Page<Voucher> vouchersPage = voucherService.getVouchers(page, size);
                MetadataDTO metadata = new MetadataDTO(
                                vouchersPage.getTotalElements(),
                                vouchersPage.getTotalPages(),
                                vouchersPage.getNumber(),
                                vouchersPage.getSize());
                return ApiMetaRes.<List<Voucher>>builder().code(1000).message("get vouchers success")
                                .result(vouchersPage.getContent()).metadata(metadata)
                                .build();
        }

}