package com.shop.fashion.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.UserSignupByEmail;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.services.AuthService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup/email")
    public ResponseEntity<ApiRes<Void>> RegisterUserByEmail(@Valid @RequestBody UserSignupByEmail userSignupByEmail)
            throws IOException {
        authService.signUpUserByEmailignUpUserByEmail(userSignupByEmail);

        return ResponseEntity.ok().body(
                ApiRes.<Void>builder().code(1000)
                        .message("Please check email to complete your registration").build());
    }

    @GetMapping("/email/confirm")
    public ResponseEntity<ApiRes<Void>> completeSignupEmail(
            @NotNull(message = "Token cannot be null") @RequestParam(value = "token") String token) {
        authService.completeSignupEmail(token);
        return ResponseEntity.ok().body(
                ApiRes.<Void>builder().code(1000)
                        .message("Register successfully. Login to experience the service")
                        .build());

    }
}
