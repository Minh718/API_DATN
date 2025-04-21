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
public class CategoryReqDTO {
    @NotNull(message = "Category name cannot be Null")
    private String name;
}