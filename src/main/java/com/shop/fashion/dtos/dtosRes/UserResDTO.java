package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResDTO {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String keyToken;
    private String password;
    private String address;
    private String picture;
    private boolean isactive = true;

}