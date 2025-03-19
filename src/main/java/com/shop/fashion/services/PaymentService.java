package com.shop.fashion.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.shop.fashion.configurations.VNPayConfig;
import com.shop.fashion.utils.VNPayUtil;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VNPayConfig vnPayConfig;

    public String createVnPayPayment(HttpServletRequest request, double amount, Long orderInfo) {
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount * 2500000d));
        vnpParamsMap.put("vnp_OrderInfo", Long.toString(orderInfo));

        // if (bankCode != null && !bankCode.isEmpty()) {
        // vnpParamsMap.put("vnp_BankCode", bankCode);
        // }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        // String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        // String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(),
        // hashData);
        String vnpSecureHash = getVnpSecureHash(vnpParamsMap);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return paymentUrl;
    }

    public String getVnpSecureHash(Map<String, String> vnpParamsMap) {
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        return VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
    }
}