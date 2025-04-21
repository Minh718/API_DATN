package com.shop.fashion.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosRes.UserInfoToken;
import com.shop.fashion.dtos.dtosRes.UserResDTO;
import com.shop.fashion.entities.User;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(ignore = true, target = "accessToken")
    @Mapping(ignore = true, target = "refreshToken")
    UserInfoToken toUserInfoToken(User user);

    UserResDTO toUserResDTO(User user);
    // UserInfo toUserInfo(User user);
}