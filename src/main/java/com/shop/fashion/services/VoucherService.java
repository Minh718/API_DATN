package com.shop.fashion.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shop.fashion.entities.User;
import com.shop.fashion.entities.UserVoucher;
import com.shop.fashion.entities.UserVoucherId;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.UserVoucherRepository;
import com.shop.fashion.repositories.VoucherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherService {
    private final UserVoucherRepository userVoucherRepository;
    private final VoucherRepository voucherRepository;

    public List<Voucher> getAllVouchers() {
        // List<Voucher> vouchers = null;
        String idUser = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Voucher> vouchers = userVoucherRepository.findAllByIdUserAndStillApply(idUser);
        return vouchers;
    }

    public Page<Voucher> getVouchers(int page, int size) {
        String idUser = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Voucher> vouchers = userVoucherRepository.findAllByIdUserAndStillApply(idUser, pageable);
        return vouchers;
    }

    public Voucher checkVoucher(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.VOUCHER_NOT_FOUND));

        if (!voucher.isActive()) {
            throw new CustomException(ErrorCode.VOUCHER_NOT_ACTIVE);
        }

        LocalDate today = LocalDate.now();

        if (voucher.getStartDate().isAfter(today)) {
            throw new CustomException(ErrorCode.BAD_REQUEST,
                    "This voucher starts on " + voucher.getStartDate().toString());
        }

        if (voucher.getEndDate().isBefore(today)) {
            throw new CustomException(ErrorCode.BAD_REQUEST,
                    "This voucher ended on " + voucher.getEndDate().toString());
        }

        return voucher;
    }

    @Async("ThreadPoolUserVoucher")
    public void addVouchersToNewUser(User user) {
        List<Voucher> vouchers = voucherRepository.findAllForNewUser();
        for (Voucher voucher : vouchers) {
            UserVoucherId userVoucherId = new UserVoucherId(user.getId(), voucher.getId());
            UserVoucher userVoucher = new UserVoucher();
            userVoucher.setId(userVoucherId);
            userVoucher.setUser(user);
            userVoucher.setVoucher(voucher);
            userVoucherRepository.save(userVoucher);
        }
    }
}