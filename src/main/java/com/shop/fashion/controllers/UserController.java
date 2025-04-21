package com.shop.fashion.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosRes.ApiMetaRes;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.MetadataDTO;
import com.shop.fashion.dtos.dtosRes.UserInfoToken;
import com.shop.fashion.dtos.dtosRes.UserResDTO;
import com.shop.fashion.dtos.dtosRes.VoucherResDTO;
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

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/all")
        public ApiMetaRes<List<UserResDTO>> getAllUserForAdmin(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
                Page<UserResDTO> usersPage = userService.getAllUserForAdmin(page, size);
                MetadataDTO metadata = new MetadataDTO(
                                usersPage.getTotalElements(),
                                usersPage.getTotalPages(),
                                usersPage.getNumber(),
                                usersPage.getSize());
                return ApiMetaRes.<List<UserResDTO>>builder().result(usersPage.getContent())
                                .message("get voucher susccess")
                                .metadata(metadata)
                                .build();
        }

        @PostAuthorize("hasRole('ADMIN')")
        @GetMapping("/active/{id}")
        public ApiRes<Void> activeUser(@PathVariable String id) {
                return ApiRes.<Void>builder().result(userService.activeUser(id))
                                .message("active user success")
                                .build();
        }

        @PostAuthorize("hasRole('ADMIN')")
        @GetMapping("/unactive/{id}")
        public ApiRes<Void> unactiveUser(@PathVariable String id) {
                return ApiRes.<Void>builder().result(userService.unactiveUser(id))
                                .message("unactive user success")
                                .build();
        }
}