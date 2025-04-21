package com.shop.fashion.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.OrderDTO;
import com.shop.fashion.dtos.dtosReq.ProductAddDTO;
import com.shop.fashion.dtos.dtosReq.SlideAddDTO;
import com.shop.fashion.dtos.dtosRes.ApiMetaRes;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.DetailOrderDTO;
import com.shop.fashion.dtos.dtosRes.MetadataDTO;
import com.shop.fashion.dtos.dtosRes.OrderResDTO;
import com.shop.fashion.dtos.dtosRes.ProductDTO;
import com.shop.fashion.entities.Slide;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.enums.OrderStatus;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.OrderMapper;
import com.shop.fashion.services.OrderService;
import com.shop.fashion.services.SlideService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/slide")
public class SlideController {
    private final SlideService slideService;

    @CacheEvict(value = "getAllPublicSlides", allEntries = true)
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<Slide> addProduct(@ModelAttribute SlideAddDTO slideDTO) {
        return ApiRes.<Slide>builder()
                .code(1000)
                .result(slideService.addSlide(slideDTO))
                .message("Add product successfully")
                .build();
    }

    @Cacheable(value = "getAllPublicSlides")
    @GetMapping("/all/public")
    public ApiRes<List<Slide>> getAllPublicSlides() {
        return ApiRes.<List<Slide>>builder().result(slideService.getAllPublicSlides())
                .message("get slides susccess")
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ApiMetaRes<List<Slide>> getAllSlides(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<Slide> slidesPage = slideService.getAllSlides(page, size);
        MetadataDTO metadata = new MetadataDTO(
                slidesPage.getTotalElements(),
                slidesPage.getTotalPages(),
                slidesPage.getNumber(),
                slidesPage.getSize());
        return ApiMetaRes.<List<Slide>>builder().result(slidesPage.getContent())
                .message("get slides susccess")
                .metadata(metadata)
                .build();
    }

    @CacheEvict(value = "getAllPublicSlides", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiRes<String> deleteSlide(@PathVariable Long id) {
        slideService.deleteSlide(id);
        return ApiRes.<String>builder().message("Delete slide successfully").code(1000).build();
    }

    @CacheEvict(value = "getAllPublicSlides", allEntries = true)

    @PostAuthorize("hasRole('ADMIN')")
    @PutMapping("/active/{id}")
    public ApiRes<Void> activeSlide(@PathVariable Long id) {
        return ApiRes.<Void>builder().result(slideService.activeSlide(id))
                .message("active voucher success")
                .build();
    }

    @CacheEvict(value = "getAllPublicSlides", allEntries = true)

    @PostAuthorize("hasRole('ADMIN')")
    @PutMapping("/unactive/{id}")
    public ApiRes<Void> unactiveSlide(@PathVariable Long id) {
        return ApiRes.<Void>builder().result(slideService.unactiveSlide(id))
                .message("active voucher success")
                .build();
    }
}