package com.shop.fashion.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shop.fashion.constants.RoleUser;
import com.shop.fashion.dtos.dtosRes.UserInfoToken;
import com.shop.fashion.dtos.dtosRes.UserResDTO;
import com.shop.fashion.dtos.dtosRes.VoucherResDTO;
import com.shop.fashion.entities.User;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.UserMapper;
import com.shop.fashion.mappers.VoucherMapper;
import com.shop.fashion.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RedisService redisService;

    public UserInfoToken getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String id = context.getAuthentication().getName();
        User user = userRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
        redisService.addLoyalUser(user.getId());
        UserInfoToken userInfo = UserMapper.INSTANCE.toUserInfoToken(user);
        return userInfo;
    }

    public String getIdAdmin() {
        Object id = redisService.getKey("idAdmin");
        if (id == null) {
            User user = userRepository.findByRolesName(RoleUser.ADMIN_ROLE)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
            redisService.setKey("idAdmin", user.getId());
            return user.getId();
        }
        return id.toString();
    }

    public Page<UserResDTO> getAllUserForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        Page<UserResDTO> voucherResDTOs = users.map(UserMapper.INSTANCE::toUserResDTO);
        return voucherResDTOs;
    }

    public Void activeUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
        user.setIsactive(true);
        userRepository.save(user);
        return null;
    }

    public Void unactiveUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
        user.setIsactive(false);
        userRepository.save(user);
        return null;

    }
}