package com.shop.fashion.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shop.fashion.constants.RoleUser;
import com.shop.fashion.dtos.dtosRes.UserInfoToken;
import com.shop.fashion.entities.User;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.UserMapper;
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
}