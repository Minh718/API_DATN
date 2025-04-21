package com.shop.fashion.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.ColorReq;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.ColorRes;
import com.shop.fashion.dtos.dtosRes.ProductColorsAdmin;
import com.shop.fashion.entities.Brand;
import com.shop.fashion.entities.Color;
import com.shop.fashion.services.BrandService;
import com.shop.fashion.services.ColorService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/brand")
public class BrandController {
    private final BrandService brandService;

    @PostMapping("/add")
    public ApiRes<Void> addBrand(@RequestBody Brand brand) {
        brandService.addBrand(brand);
        return ApiRes.<Void>builder().code(1003).message("brand brand is success")
                .build();
    }

    @GetMapping("/all")
    public ApiRes<List<Brand>> getALlBrands() {
        return ApiRes.<List<Brand>>builder().code(1003).result(brandService.getALlBrands())
                .message("get all brands is success")
                .build();
    }
}