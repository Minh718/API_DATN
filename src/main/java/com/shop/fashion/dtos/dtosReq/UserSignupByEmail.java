package com.shop.fashion.dtos.dtosReq;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * dtoRegisterEmail
 */
@Getter
@Setter
public class UserSignupByEmail {
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email is not valid")
    private String email;
    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "Password must contain at least one letter and one number")
    private String password;
}