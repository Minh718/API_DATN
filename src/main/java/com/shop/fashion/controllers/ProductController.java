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
import com.shop.fashion.dtos.dtosRes.ApiMetaRes;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.MetadataDTO;
import com.shop.fashion.dtos.dtosRes.ProductDTO;
import com.shop.fashion.dtos.dtosRes.ProductDetailDTO;
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

        @GetMapping("/search")
        public ApiMetaRes<List<ProductDTO>> searchProducts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdDate") String sortBy,
                        @RequestParam(defaultValue = "desc") String order,
                        @RequestParam String query) {
                Page<ProductDTO> productPage = productService.searchPublicProductsByName(query, size, page, sortBy,
                                order);
                MetadataDTO metadata = new MetadataDTO(
                                productPage.getTotalElements(),
                                productPage.getTotalPages(),
                                productPage.getNumber(),
                                productPage.getSize());
                return ApiMetaRes.<List<ProductDTO>>builder().code(1000).message("lấy danh sách thành công")
                                .result(productPage.getContent()).metadata(metadata).build();
        }

        @GetMapping("/draft")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ApiMetaRes<List<ProductDTO>> getDraftProducts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Page<ProductDTO> productPage = productService.getDraftProducts(page, size);
                MetadataDTO metadata = new MetadataDTO(
                                productPage.getTotalElements(),
                                productPage.getTotalPages(),
                                productPage.getNumber(),
                                productPage.getSize());

                return ApiMetaRes.<List<ProductDTO>>builder().code(1000).message("lấy danh sách thành công")
                                .result(productPage.getContent()).metadata(metadata).build();

        }

        @Cacheable(value = "publicProductsBySubCategory", key = "#page + '-' + #size + '-' + #thump + '-' + #sortBy + '-' + #order", condition = "#page == 0")
        @GetMapping("/public/subCategory")
        public ApiMetaRes<List<ProductDTO>> getPublicProductsBySubCategory(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam String thump,
                        @RequestParam(defaultValue = "createdDate") String sortBy,
                        @RequestParam(defaultValue = "desc") String order,
                        @RequestParam Double minPrice, // Added
                        @RequestParam Double maxPrice) {

                Page<ProductDTO> productPage = productService.getPublicProductsBySubCategory(page, size, thump, sortBy,
                                order, minPrice, maxPrice);
                MetadataDTO metadata = new MetadataDTO(
                                productPage.getTotalElements(),
                                productPage.getTotalPages(),
                                productPage.getNumber(),
                                productPage.getSize());
                return ApiMetaRes.<List<ProductDTO>>builder()
                                .code(1000)
                                .message("lấy danh sách thành công")
                                .result(productPage.getContent())
                                .metadata(metadata)
                                .build();
        }

        @GetMapping("/public")
        public ApiMetaRes<List<ProductDTO>> getPublicProducts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdDate") String sortBy,
                        @RequestParam(defaultValue = "desc") String order,
                        @RequestParam Double minPrice, // Added
                        @RequestParam Double maxPrice) {

                Page<ProductDTO> productPage = productService.getPublicProducts(page, size, sortBy,
                                order, minPrice, maxPrice);
                MetadataDTO metadata = new MetadataDTO(
                                productPage.getTotalElements(),
                                productPage.getTotalPages(),
                                productPage.getNumber(),
                                productPage.getSize());
                return ApiMetaRes.<List<ProductDTO>>builder()
                                .code(1000)
                                .message("lấy danh sách thành công")
                                .result(productPage.getContent())
                                .metadata(metadata)
                                .build();
        }

        @GetMapping("/{id}")
        public ApiRes<ProductDetailDTO> getProductDetail(@PathVariable long id) {
                return ApiRes.<ProductDetailDTO>builder().code(1000)
                                .message("get product detail success")
                                .result(productService.getProductDetail(id)).build();
        }
}