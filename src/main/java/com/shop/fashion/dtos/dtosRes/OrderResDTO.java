package com.shop.fashion.dtos.dtosRes;

import com.shop.fashion.entities.Voucher;
import com.shop.fashion.enums.OrderStatus;
import com.shop.fashion.enums.ShippingStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Order
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResDTO {

    private Long id;
    private long totalAmount;
    private Long discount;
    private String shippingAddress;
    private String phone;
    private String fullName;
    private String urlPayment;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private ShippingStatus shippingStatus;

    private String trackingNumber;
    private String createdAt;
    private VoucherResDTO voucher;
    PaymentDTO payment;

}