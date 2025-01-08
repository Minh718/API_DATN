package com.shop.fashion.dtos.dtosRes;

import lombok.Getter;
import lombok.Setter;

/**
 * ApiErrorRes
 */
@Getter
@Setter
public class ApiErrorRes {

    private int code;
    private String message;
}