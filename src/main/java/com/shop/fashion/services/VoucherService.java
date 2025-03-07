package com.shop.fashion.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shop.fashion.entities.Voucher;
import com.shop.fashion.repositories.UserVoucherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherService {
    UserVoucherRepository userVoucherRepository;

    public Page<Voucher> getVouchers(int page, int size) {
        String idUser = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Voucher> vouchers = userVoucherRepository.findAllByIdUserAndStillApply(idUser, pageable);
        return vouchers;
    }
}