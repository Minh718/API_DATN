package com.shop.fashion.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.CartAddProductDTO;
import com.shop.fashion.dtos.dtosRes.CartProductSizeColorDTO;
import com.shop.fashion.dtos.dtosRes.ProductSizeColorDTO;
import com.shop.fashion.entities.Cart;
import com.shop.fashion.entities.CartProductSizeColor;
import com.shop.fashion.entities.ProductSizeColor;
import com.shop.fashion.entities.User;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.CartProductSizeColorMapper;
import com.shop.fashion.mappers.ProductSizeColorMapper;
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

    public Void removeProductFromCart(long cartProductSizeColorId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByIdWithCart(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
        Cart cart = user.getCart();
        CartProductSizeColor cartProductSizeColor = cartProductSizeColorRepository
                .findByIdAndCartId(cartProductSizeColorId, cart.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CART_PRODUCT_SIZE_NOT_EXISTED));
        cartProductSizeColorRepository.delete(cartProductSizeColor);
        return null;
    }

    public Page<CartProductSizeColorDTO> getAllProductOfCart(int page, int size) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByIdWithCart(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));

        Pageable pageable = PageRequest.of(page, size);

        Page<CartProductSizeColor> cartProductSizeColors = cartProductSizeColorRepository
                .findAllByCartIdOrderByUpdateAtDesc(
                        user.getCart().getId(), pageable);

        List<CartProductSizeColorDTO> dtoList = new ArrayList<>();

        for (CartProductSizeColor cartProductSizeColor : cartProductSizeColors) {
            var availableQuantity = redisService
                    .getKey("productSizeColor:" + cartProductSizeColor.getProductSizeColorId());

            if (availableQuantity == null || (int) availableQuantity <= 0) {
                cartProductSizeColorRepository.delete(cartProductSizeColor);
                continue;
            } else if (cartProductSizeColor.getQuantity() > (int) availableQuantity) {
                cartProductSizeColor.setQuantity((int) availableQuantity);
                cartProductSizeColorRepository.save(cartProductSizeColor);
            }
            CartProductSizeColorDTO dto = CartProductSizeColorMapper.INSTANCE
                    .toCartProductSizeColorDTO(cartProductSizeColor);

            ProductSizeColor productSizeColor = productSizeColorRepository
                    .findByIdFetchProductSizeAndFetchProduct(
                            cartProductSizeColor.getProductSizeColorId())
                    .orElseThrow(() -> new CustomException(
                            ErrorCode.PRODUCT_SIZE_COLOR_NOT_EXISTED));

            ProductSizeColorDTO productSizeColorDTO = ProductSizeColorMapper.INSTANCE
                    .toProductSizeColorDTO(productSizeColor);

            productSizeColorDTO.setQuantity((int) availableQuantity);

            int quantityProductSize = (int) redisService.getKey(
                    "productSize:" + productSizeColor.getProductSize().getId());

            productSizeColorDTO.getProductSize().setQuantity(quantityProductSize);
            dto.setProductSizeColor(productSizeColorDTO);
            dtoList.add(dto);
        }

        return new PageImpl<>(dtoList, pageable,
                cartProductSizeColors.getTotalElements());
    }
}