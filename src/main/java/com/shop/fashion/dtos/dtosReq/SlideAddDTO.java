package com.shop.fashion.dtos.dtosReq;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

public record SlideAddDTO(@NotNull(message = "Title cannot be Null") String title,
        @NotNull(message = "Description is required") String description,
        Boolean status,
        @NotNull(message = "image product cannot be Null") MultipartFile file) {
};