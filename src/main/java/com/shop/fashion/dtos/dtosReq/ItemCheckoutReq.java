package com.shop.fashion.dtos.dtosReq;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemCheckoutReq {
    @NotNull(message = "idCartProductSize is required")
    private Long id;
    @NotNull(message = "quantity is required")
    private int quantity;
}
