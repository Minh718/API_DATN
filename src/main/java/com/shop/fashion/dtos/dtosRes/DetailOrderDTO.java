package com.shop.fashion.dtos.dtosRes;

import java.util.Set;

import com.shop.fashion.entities.Voucher;
import com.shop.fashion.enums.OrderStatus;
import com.shop.fashion.enums.ShippingStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetailOrderDTO {
    private Long id;
    private double totalAmount;
    private double discount;
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
    PaymentDTO payment;
    VoucherResDTO voucher;
    Set<OrderProductResDTO> orderProducts;
}
