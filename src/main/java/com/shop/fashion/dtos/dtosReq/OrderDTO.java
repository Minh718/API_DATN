package com.shop.fashion.dtos.dtosReq;

import com.shop.fashion.dtos.dtosRes.CheckoutRes;
import com.shop.fashion.enums.PaymentMethod;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    private String shippingAddress;

    private String phone;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private CheckoutRes checkoutRes;
    private CheckoutReq checkoutReq;

}