package com.shop.fashion.dtos.dtosReq;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductSizeColorQuantityDTO {
    @NotNull(message = "idProductSizeColor cannot be Null")
    private long pscId;

    @NotNull(message = "quantity cannot be Null")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;
}