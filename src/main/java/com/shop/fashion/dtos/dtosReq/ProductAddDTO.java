package com.shop.fashion.dtos.dtosReq;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

public record ProductAddDTO(
        @NotNull(message = "name product cannot be Null") String name,
        @NotNull(message = "price product is required") int price,
        @NotNull(message = "percent is required") int percent,
        @NotNull(message = "subCate is required") Long subCate_id,
        @NotNull(message = "brand is required") Long brand_id,
        @NotNull(message = "status is required") boolean status,
        @NotNull(message = "image product cannot be Null") MultipartFile file,
        String description,
        String model,
        String material,
        String origin,
        String warranty,
        String madeIn,
        List<MultipartFile> files) {
};
