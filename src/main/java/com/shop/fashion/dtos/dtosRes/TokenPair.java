package com.shop.fashion.dtos.dtosRes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenPair {
    private String accessToken;
    private String refreshToken;
}
