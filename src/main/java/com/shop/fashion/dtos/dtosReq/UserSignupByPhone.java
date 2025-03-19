package com.shop.fashion.dtos.dtosReq;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSignupByPhone {
    @NotNull(message = "Email cannot be null")
    private String phone;
    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "Password must contain at least one letter and one number")
    private String password;
    @Size(min = 6, message = "Password must be 6 number")
    @NotNull(message = "code cannot be null")
    private String otp;

}