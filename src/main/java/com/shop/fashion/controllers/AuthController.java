package com.shop.fashion.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.UserSignin;
import com.shop.fashion.dtos.dtosReq.UserSignupByEmail;
import com.shop.fashion.dtos.dtosReq.UserSignupByPhone;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.UserInfoToken;
import com.shop.fashion.services.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
        private final AuthService authService;

        @NonFinal
        @Value("${frontend_host:http://localhost:5173")
        private String frontend_host;

        @PostMapping("/signup/email")
        public ResponseEntity<ApiRes<Void>> userSignupByEmail(@Valid @RequestBody UserSignupByEmail userSignupByEmail)
                        throws IOException {
                authService.handleUserSignupByEmail(userSignupByEmail);

                return ResponseEntity.ok().body(
                                ApiRes.<Void>builder().code(1000)
                                                .message("Please check email to complete your registration").build());
        }

        @PostMapping("/signup/email")
        public ResponseEntity<ApiRes<Void>> userSignupByPhone(@Valid @RequestBody UserSignupByPhone userSignupByPhone)
                        throws IOException {
                authService.handleUserSignupByPhone(userSignupByPhone);

                return ResponseEntity.ok().body(
                                ApiRes.<Void>builder().code(1000)
                                                .message("Register successfully").build());
        }

        @GetMapping("/email/getotp/{phone}")
        public ResponseEntity<ApiRes<Void>> handleGetOtpForPhoneNumber(
                        @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits") @PathVariable String phone)
                        throws IOException {
                authService.handleGetOtpForPhoneNumber(phone);

                return ResponseEntity.ok().body(
                                ApiRes.<Void>builder().code(1000)
                                                .message("Get otp successfully").build());
        }

        @GetMapping("/email/confirm")
        public void completeSignupEmail(
                        @NotNull(message = "Token cannot be null") @RequestParam String token,
                        HttpServletResponse response) throws IOException {
                authService.completeSignupEmail(token);
                response.sendRedirect(frontend_host + "/login");
        }

        @PostMapping("/signin")
        public ResponseEntity<ApiRes<UserInfoToken>> userSigninbyUsername(@Valid @RequestBody UserSignin userSignin) {
                ApiRes<UserInfoToken> res = ApiRes.<UserInfoToken>builder()
                                .code(1000)
                                .message("Signin successfully")
                                .result(authService.userSigninbyUsername(userSignin))
                                .build();
                return ResponseEntity.ok().body(res);
        }

        @PostMapping("/admin/signin")
        public ResponseEntity<ApiRes<UserInfoToken>> adminSignIn(@Valid @RequestBody UserSignin userSignin) {
                ApiRes<UserInfoToken> res = ApiRes.<UserInfoToken>builder()
                                .code(1000)
                                .message("Signin successfully")
                                .result(authService.adminSignin(userSignin))
                                .build();
                return ResponseEntity.ok().body(res);
        }

        @PostMapping("/signin/google")
        public ApiRes<UserInfoToken> userSigninByGoogle(
                        @NotNull(message = "code is requried") @RequestParam() String code) {
                return ApiRes.<UserInfoToken>builder()
                                .code(1000)
                                .message("Signin by google successfully")
                                .result(authService.userSigninByGoogle(code))
                                .build();
        }
}
