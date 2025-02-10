package com.shop.fashion.dtos.dtosRes;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * UserInfo
 */
@Getter
@Setter
@Builder
public class UserInfoToken {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String picture;
    private String accessToken;
    private String refreshToken;
}