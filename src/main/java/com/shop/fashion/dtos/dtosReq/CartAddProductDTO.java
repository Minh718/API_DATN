package com.shop.fashion.dtos.dtosReq;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartAddProductDTO {
    @NotNull(message = "quantity is required")
    private int quantity;
    @NotNull(message = "idproductSizeColor is required")
    private long productSizeColorId;
}
