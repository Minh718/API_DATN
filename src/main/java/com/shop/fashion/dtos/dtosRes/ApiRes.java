package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApiRes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRes<T> {

    private int code;
    private String message;
    private T result;
}