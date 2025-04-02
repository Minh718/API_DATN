package com.shop.fashion.controllers;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.CategoryDTO;
import com.shop.fashion.services.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/category")
public class CategoryController {
    private final CategoryService categoryService;

    @Cacheable(value = "getAllCategories")
    @GetMapping("/all")
    public ApiRes<List<CategoryDTO>> getAllCategories() {
        return ApiRes.<List<CategoryDTO>>builder().code(1003).message("success")
                .result(categoryService.getAllCategories())
                .build();
    }

}