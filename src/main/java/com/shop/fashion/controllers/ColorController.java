package com.shop.fashion.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.ColorReq;
import com.shop.fashion.dtos.dtosRes.ApiMetaRes;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.ColorTable;
import com.shop.fashion.dtos.dtosRes.ColorRes;
import com.shop.fashion.dtos.dtosRes.MetadataDTO;
import com.shop.fashion.dtos.dtosRes.ProductColorsAdmin;
import com.shop.fashion.entities.Color;
import com.shop.fashion.services.ColorService;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/color")
public class ColorController {
    private final ColorService colorService;

    @PostMapping("/add")
    public ApiRes<ColorTable> addColor(@RequestBody ColorReq color) {
        return ApiRes.<ColorTable>builder().code(1003).message("add color is success")
                .result(colorService.addColor(color))
                .build();
    }

    @GetMapping("/{idProductSize}")
    public ApiRes<List<ColorRes>> findAllColorNotInProductSize(@PathVariable Long idProductSize) {
        return ApiRes.<List<ColorRes>>builder().code(1003).message("get colors successfully")
                .result(colorService.findAllColorNotInProductSize(idProductSize)).build();
    }

    @GetMapping("/productSize/{id}")
    public ApiRes<List<ProductColorsAdmin>> getAllColorsOfProduct(@PathVariable Long id) {
        return ApiRes.<List<ProductColorsAdmin>>builder().code(1003).message("get colors successfully")
                .result(colorService.getAllColorsOfProduct(id)).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ApiMetaRes<List<ColorTable>> getAllColorForAdmin(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<ColorTable> colorsPage = colorService.getAllColorsForAdmin(page, size);
        MetadataDTO metadata = new MetadataDTO(
                colorsPage.getTotalElements(),
                colorsPage.getTotalPages(),
                colorsPage.getNumber(),
                colorsPage.getSize());
        return ApiMetaRes.<List<ColorTable>>builder().result(colorsPage.getContent())
                .message("get colors susccess")
                .metadata(metadata)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<Void> deleteColor(@PathVariable Long id) {
        return ApiRes.<Void>builder().code(1003).message("delete color is success")
                .result(colorService.deleteColor(id))
                .build();
    }
}
