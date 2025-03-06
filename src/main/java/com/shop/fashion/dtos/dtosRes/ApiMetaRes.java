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
@AllArgsConstructor
@NoArgsConstructor
public class ApiMetaRes<T> {

    private int code;
    private String message;
    private T result;
    private MetadataDTO metadata;
}