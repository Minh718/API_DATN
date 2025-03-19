package com.shop.fashion.services;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.CheckoutReq;
import com.shop.fashion.dtos.dtosReq.ItemCheckoutReq;
import com.shop.fashion.dtos.dtosRes.CheckoutRes;
import com.shop.fashion.entities.CartProductSizeColor;
import com.shop.fashion.entities.Product;
import com.shop.fashion.entities.ProductSizeColor;
import com.shop.fashion.entities.UserVoucher;
import com.shop.fashion.entities.UserVoucherId;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.enums.TypeVoucher;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.CartProductSizeColorRepository;
import com.shop.fashion.repositories.ProductSizeColorRepository;
import com.shop.fashion.repositories.UserVoucherRepository;

import lombok.RequiredArgsConstructor;

/**
 * CheckoutService
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final CartProductSizeColorRepository cartProductSizeColorRepository;
    private final VoucherService voucherService;
    private final UserVoucherRepository userVoucherRepository;
    private final RedisService redisService;
    private final ProductSizeColorRepository productSizeColorRepository;

    public CheckoutRes checkoutProducts(CheckoutReq checkoutReq) {
        double totalPrice = 0;
        CheckoutRes checkoutRes = new CheckoutRes();
        for (ItemCheckoutReq cps : checkoutReq.getItems()) {
            CartProductSizeColor cpscdb = cartProductSizeColorRepository.findById(cps.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PLEASE_RELOAD_PAGE));
            if (cpscdb.getQuantity() != cps.getQuantity())
                throw new CustomException(ErrorCode.PLEASE_RELOAD_PAGE);

            var availableQuantity = redisService.getKey("productSizeColor:" + cpscdb.getProductSizeColorId());
            ProductSizeColor productSizeColor = productSizeColorRepository
                    .findByIdFetchProductSizeAndFetchProduct(cpscdb.getProductSizeColorId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_SIZE_COLOR_NOT_EXISTED));
            if (availableQuantity == null || (int) availableQuantity < cps.getQuantity())
                throw new CustomException(ErrorCode.PLEASE_RELOAD_PAGE);
            Product product = productSizeColor.getProductSize().getProduct();
            totalPrice += (product.getPrice() * (100 - product.getPercent())) *
                    cpscdb.getQuantity();
            System.out.println("totalPrice: " + totalPrice);
        }
        totalPrice = Math.round(totalPrice) / 100.0;
        checkoutRes.setTotalPrice(totalPrice);
        checkoutRes.setPaymentFee(totalPrice);
        if (checkoutReq.getCode() != null)
            applyVoucher(checkoutReq.getCode(), totalPrice, checkoutRes);
        return checkoutRes;
    }

    private void applyVoucher(String code, double totalPrice, CheckoutRes checkoutRes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Voucher voucher = voucherService.checkVoucher(code);
        UserVoucherId userVoucherId = new UserVoucherId(userId, voucher.getId());
        UserVoucher userVoucher = userVoucherRepository.findById(userVoucherId)
                .orElseThrow(() -> new CustomException(ErrorCode.VOUCHER_DONT_BELONG_TO_USER));
        if (userVoucher.isUsed()) {
            throw new CustomException(ErrorCode.VOUCHER_IS_USED_ALREADY);
        }
        if (totalPrice < voucher.getMinPrice())
            throw new CustomException(ErrorCode.BAD_REQUEST, "This voucher only apply for voucher with minimum "
                    + "$" + voucher.getMinPrice());
        double discount = 0;
        if (voucher.getType() == TypeVoucher.FIXED) {
            discount = voucher.getDiscount();
        } else if (voucher.getType() == TypeVoucher.PERCENT) {
            discount = totalPrice * voucher.getDiscount() / 100;
            if (discount > voucher.getMaxDiscount())
                discount = voucher.getMaxDiscount();
        }
        discount = Math.round(discount);
        checkoutRes.setDiscount(discount);
        checkoutRes.setPaymentFee(totalPrice - discount);

    }

}