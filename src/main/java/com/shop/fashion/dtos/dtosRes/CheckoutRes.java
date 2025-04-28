package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CheckoutRes
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRes {
    private double totalPrice;
    private double paymentFee;
    private double discount = 0;
}