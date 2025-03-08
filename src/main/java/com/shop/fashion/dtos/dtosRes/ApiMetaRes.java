package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ApiRes
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiMetaRes<T> {

    private int code;
    private String message;
    private T result;
    private MetadataDTO metadata;
}