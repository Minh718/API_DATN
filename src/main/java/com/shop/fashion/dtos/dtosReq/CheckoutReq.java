package com.shop.fashion.dtos.dtosReq;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutReq {

    private String code;
    private Set<ItemCheckoutReq> items;

}