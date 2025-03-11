package com.shop.fashion.services;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.CartAddProductDTO;
import com.shop.fashion.entities.Cart;
import com.shop.fashion.entities.CartProductSizeColor;
import com.shop.fashion.entities.ProductSizeColor;
import com.shop.fashion.entities.User;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.CartProductSizeColorRepository;
import com.shop.fashion.repositories.ProductSizeColorRepository;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.utils.DateTimeUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final ProductSizeColorRepository productSizeColorRepository;
    private final CartProductSizeColorRepository cartProductSizeColorRepository;

    public Void addProductToCart(CartAddProductDTO cartAddProductDTO) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByIdWithCart(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
        Cart cart = user.getCart();

        int requestedQuantity = cartAddProductDTO.getQuantity();

        ProductSizeColor productSizeColor = productSizeColorRepository
                .findById(cartAddProductDTO.getProductSizeColorId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_SIZE_NOT_EXISTED));

        var availableQuantity = redisService
                .getKey("productSizeColor:" + cartAddProductDTO.getProductSizeColorId());

        if (availableQuantity == null) {
            redisService.setKey("productSizeColor:" +
                    cartAddProductDTO.getProductSizeColorId(), 0);
            throw new CustomException(ErrorCode.QUANTITY_NOT_ENOUGH);
        } else if ((int) availableQuantity < requestedQuantity) {
            throw new CustomException(ErrorCode.QUANTITY_NOT_ENOUGH);
        }
        CartProductSizeColor cartProductSizeColor = cartProductSizeColorRepository
                .findByCartIdAndProductSizeColorId(cart.getId(),
                        cartAddProductDTO.getProductSizeColorId())
                .orElseGet(() -> {
                    return CartProductSizeColor.builder().cart(cart)
                            .productSizeColorId(productSizeColor.getId())
                            .build();
                });
        cartProductSizeColor.setQuantity(requestedQuantity);
        cartProductSizeColor.setUpdateAt(DateTimeUtil.getCurrentVietnamTime());
        cartProductSizeColorRepository.save(cartProductSizeColor);
        return null;
    }
}