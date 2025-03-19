package com.shop.fashion.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.CheckoutReq;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.CheckoutRes;
import com.shop.fashion.services.CheckoutService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * CheckoutController
 */
@RestController
@RequestMapping("api/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final CheckoutService checkoutService;

    @PostMapping("/products")
    public ApiRes<CheckoutRes> checkoutProducts(@Valid @RequestBody CheckoutReq checkoutReq) {
        return ApiRes.<CheckoutRes>builder().code(1000).message("Success")
                .result(checkoutService.checkoutProducts(checkoutReq)).build();
    }

}