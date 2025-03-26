package com.shop.fashion.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.UserInfoToken;
import com.shop.fashion.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * UserController
 */
@RequestMapping("api/user")
@RequiredArgsConstructor
@RestController
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/my-info")

    public ApiRes<UserInfoToken> getInfoUser() {
        log.info("Get user info");
        return ApiRes.<UserInfoToken>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-info")
    public ApiRes<UserInfoToken> getInfoAdmin() {
        log.info("Get user info");
        return ApiRes.<UserInfoToken>builder()
                .result(userService.getMyInfo())
                .build();
    }
}