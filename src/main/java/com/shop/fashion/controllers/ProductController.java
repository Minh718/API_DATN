package com.shop.fashion.controllers;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.ProductAddDTO;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.ProductDTO;
import com.shop.fashion.services.ProductService;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ProductController
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/product")
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<ProductDTO> addProduct(@ModelAttribute ProductAddDTO productDTO) {
        log.info("admin add new product" + productDTO.name());
        return ApiRes.<ProductDTO>builder()
                .code(1000)
                .result(productService.addProduct(productDTO))
                .message("Add product successfully")
                .build();
    }

    @DeleteMapping("/{idProduct}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiRes<Void>> deleteProduct(
            @NotNull(message = "idProduct is required") @PathVariable Long idProduct) {
        log.info("admin delete product" + idProduct);
        productService.deleteProduct(idProduct);
        return ResponseEntity.ok().body(ApiRes.<Void>builder()
                .code(1000)
                .message("delete sản phẩm thành công")
                .build());
    }
}