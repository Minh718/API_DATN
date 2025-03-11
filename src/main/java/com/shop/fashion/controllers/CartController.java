package com.shop.fashion.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.CartAddProductDTO;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.services.CartService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/cart")
public class CartController {
    private final CartService cartService;

    @PostMapping("/add")
    public ApiRes<Void> addProductToCart(@RequestBody CartAddProductDTO cartAddProductDTO) {
        return ApiRes.<Void>builder().code(1000).message("add product to cart success")
                .result(cartService.addProductToCart(cartAddProductDTO))
                .build();
    }

}