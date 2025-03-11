package com.shop.fashion.dtos.dtosReq;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddProductDTO {
    @NotNull(message = "quantity is required")
    private int quantity;
    @NotNull(message = "idproductSizeColor is required")
    private long productSizeColorId;
}
