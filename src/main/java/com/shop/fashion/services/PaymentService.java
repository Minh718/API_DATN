package com.shop.fashion.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.shop.fashion.configurations.VNPayConfig;
import com.shop.fashion.dtos.dtosRes.PaymentDetailDTO;
import com.shop.fashion.entities.Payment;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.PaymentRepository;
import com.shop.fashion.utils.VNPayUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;

    public String createVnPayPayment(HttpServletRequest request, double amount, Long orderInfo) {
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();

        double exchangeRate = 25000d;
        long vndAmount = Math.round(amount * exchangeRate); // USD -> VND
        long vnpAmount = vndAmount * 100; // Multiply by 100 as VNPAY expects smallest unit

        vnpParamsMap.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParamsMap.put("vnp_OrderInfo", String.valueOf(orderInfo));
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        List<String> fieldNames = new ArrayList<>(vnpParamsMap.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder queryUrl = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParamsMap.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                String encodedName = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
                hashData.append(encodedName).append('=').append(encodedValue).append('&');
                queryUrl.append(encodedName).append('=').append(encodedValue).append('&');
            }
        }

        if (hashData.length() > 0)
            hashData.setLength(hashData.length() - 1);
        if (queryUrl.length() > 0)
            queryUrl.setLength(queryUrl.length() - 1);

        String secureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());

        queryUrl.append("&vnp_SecureHash=").append(secureHash);

        return vnPayConfig.getVnp_PayUrl() + "?" + queryUrl.toString();
    }

    public String getVnpSecureHash(Map<String, String> vnpParamsMap) {
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        return VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
    }

    public PaymentDetailDTO getPaymentDetail(String tranid) {
        Payment payment = paymentRepository.findByTransactionID(tranid)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        return PaymentDetailDTO.builder().amount(payment.getAmount()).transactionID(payment.getTransactionID())
                .paymentMethod(payment.getPaymentMethod()).orderId(payment.getOrderId()).build();
    }
}