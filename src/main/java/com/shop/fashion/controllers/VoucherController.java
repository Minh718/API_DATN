package com.shop.fashion.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.VoucherDTO;
import com.shop.fashion.dtos.dtosRes.ApiMetaRes;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.MetadataDTO;
import com.shop.fashion.dtos.dtosRes.VoucherResDTO;
import com.shop.fashion.entities.Slide;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.services.VoucherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vouchers")
public class VoucherController {
        private final VoucherService voucherService;

        @GetMapping("/user/all")
        public ApiRes<List<VoucherResDTO>> getAllVouchers() {
                return ApiRes.<List<VoucherResDTO>>builder().result(voucherService.getAllVouchers())
                                .message("get vouchers susccess")
                                .build();
        }

        @GetMapping("/user/get")
        public ApiMetaRes<List<VoucherResDTO>> getVouchers(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
                List<VoucherResDTO> vouchersPage = voucherService.getVouchers(page, size);
                // MetadataDTO metadata = new MetadataDTO(
                // vouchersPage.getTotalElements(),
                // vouchersPage.getTotalPages(),
                // vouchersPage.getNumber(),
                // vouchersPage.getSize());
                return ApiMetaRes.<List<VoucherResDTO>>builder().code(1000).message("get vouchers success")
                                .result(vouchersPage)
                                .build();
        }

        @PostAuthorize("hasRole('ADMIN')")
        @PostMapping("/create")
        public ApiRes<VoucherResDTO> createVoucher(@RequestBody VoucherDTO voucherDTO) {
                return ApiRes.<VoucherResDTO>builder().result(voucherService.createVoucher(voucherDTO))
                                .message("create voucher success")
                                .build();
        }

        @PostAuthorize("hasRole('ADMIN')")
        @PutMapping("")
        public ApiRes<VoucherResDTO> updateVoucher(@RequestBody VoucherResDTO voucherDTO) {
                return ApiRes.<VoucherResDTO>builder().result(voucherService.updateVoucher(voucherDTO))
                                .message("update voucher success")
                                .build();
        }

        @PostAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        public ApiRes<VoucherResDTO> deleteVoucher(@PathVariable Long id) {
                return ApiRes.<VoucherResDTO>builder().result(voucherService.deleteVoucher(id))
                                .message("delete voucher success")
                                .build();
        }

        @PostAuthorize("hasRole('ADMIN')")
        @GetMapping("/active/{id}")
        public ApiRes<Void> activeVoucher(@PathVariable Long id) {
                return ApiRes.<Void>builder().result(voucherService.activeVoucher(id))
                                .message("active voucher success")
                                .build();
        }

        @PostAuthorize("hasRole('ADMIN')")
        @GetMapping("/unactive/{id}")
        public ApiRes<Void> unactiveVoucher(@PathVariable Long id) {
                return ApiRes.<Void>builder().result(voucherService.unactiveVoucher(id))
                                .message("unactive voucher success")
                                .build();
        }

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/all")
        public ApiMetaRes<List<VoucherResDTO>> getAllVoucherForAdmin(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
                Page<VoucherResDTO> vouchersPage = voucherService.getAllVoucherForAdmin(page, size);
                MetadataDTO metadata = new MetadataDTO(
                                vouchersPage.getTotalElements(),
                                vouchersPage.getTotalPages(),
                                vouchersPage.getNumber(),
                                vouchersPage.getSize());
                return ApiMetaRes.<List<VoucherResDTO>>builder().result(vouchersPage.getContent())
                                .message("get voucher susccess")
                                .metadata(metadata)
                                .build();
        }
}