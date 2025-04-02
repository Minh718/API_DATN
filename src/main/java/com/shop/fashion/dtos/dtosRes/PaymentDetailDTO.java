package com.shop.fashion.dtos.dtosRes;

import java.util.List;

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
@Builder
public class PaymentDetailDTO {
    private long amount;
    private String transactionID;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Long orderId;
}