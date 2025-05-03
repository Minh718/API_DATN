package com.shop.fashion.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shop.fashion.dtos.dtosReq.VoucherDTO;
import com.shop.fashion.dtos.dtosRes.ProductDTO;
import com.shop.fashion.dtos.dtosRes.VoucherResDTO;
import com.shop.fashion.entities.Slide;
import com.shop.fashion.entities.User;
import com.shop.fashion.entities.UserVoucher;
import com.shop.fashion.entities.UserVoucherId;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.enums.VoucherTargetType;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.ProductMapper;
import com.shop.fashion.mappers.VoucherMapper;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.repositories.UserVoucherRepository;
import com.shop.fashion.repositories.VoucherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherService {
    private final UserVoucherRepository userVoucherRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final RedisService redisService;
    @Autowired
    @Lazy
    private VoucherService self;

    public List<VoucherResDTO> getAllVouchers() {
        String idUser = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Voucher> vouchers = userVoucherRepository.findAllByIdUserAndStillApply(idUser);
        return VoucherMapper.INSTANCE.toVoucherResDTOs(vouchers);
    }

    public Page<VoucherResDTO> getAllVoucherForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voucher> vouchers = voucherRepository.findAll(pageable);
        Page<VoucherResDTO> voucherResDTOs = vouchers.map(VoucherMapper.INSTANCE::toVoucherResDTO);
        return voucherResDTOs;
    }

    public List<VoucherResDTO> getVouchers(int page, int size) {
        String idUser = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        List<Voucher> vouchers = userVoucherRepository.findAllByIdUserAndStillApplyPageable(idUser, pageable);
        return VoucherMapper.INSTANCE.toVoucherResDTOs(vouchers);
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

    @Transactional
    public VoucherResDTO createVoucher(VoucherDTO voucherDTO) {
        Voucher voucher = VoucherMapper.INSTANCE.toVoucher(voucherDTO);
        voucherRepository.save(voucher);
        List<UserVoucher> userVouchers = new ArrayList<>();

        if (voucherDTO.getTargetUserType() == VoucherTargetType.GLOBAL) {
            List<User> allUsers = userRepository.findAll();

            userVouchers = addVoucherToUsers(voucher, allUsers);
        } else if (voucherDTO.getTargetUserType() == VoucherTargetType.NEW_USER) {
            voucher.setForNewUser(true);
        } else {
            List<Object> idUserLoyersObjects = redisService.getStringArray("loyalUser");
            List<String> idUserLoyers = idUserLoyersObjects.stream()
                    .map(object -> (String) object)
                    .collect(Collectors.toList());
            List<User> users = userRepository.findAllById(idUserLoyers);
            userVouchers = addVoucherToUsers(voucher, users);
        }
        voucher.setUserVouchers(userVouchers);
        return VoucherMapper.INSTANCE.toVoucherResDTO(voucher);
    }

    public VoucherResDTO updateVoucher(VoucherResDTO voucherDTO) {
        Voucher voucher = VoucherMapper.INSTANCE.toVoucher(voucherDTO);
        Voucher res = voucherRepository.save(voucher);
        return VoucherMapper.INSTANCE.toVoucherResDTO(res);
    }

    public List<UserVoucher> addVoucherToUsers(Voucher voucher, List<User> users) {
        List<UserVoucher> userVouchers = new ArrayList<>();
        for (User user : users) {
            UserVoucherId userVoucherId = new UserVoucherId(user.getId(), voucher.getId());
            UserVoucher userVoucher = new UserVoucher();
            userVoucher.setId(userVoucherId);
            userVoucher.setUser(user);
            userVoucher.setVoucher(voucher);
            userVouchers.add(userVoucher);
        }
        return userVouchers;
    }

    public VoucherResDTO deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.VOUCHER_NOT_FOUND));
        voucherRepository.delete(voucher);
        return VoucherMapper.INSTANCE.toVoucherResDTO(voucher);
    }

    public Void activeVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.VOUCHER_NOT_FOUND));
        voucher.setActive(true);
        voucherRepository.save(voucher);
        return null;
    }

    public Void unactiveVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.VOUCHER_NOT_FOUND));
        voucher.setActive(false);
        voucherRepository.save(voucher);
        return null;

    }
}