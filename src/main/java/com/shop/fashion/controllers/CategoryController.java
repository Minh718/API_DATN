package com.shop.fashion.controllers;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.AddSizeToCategoryReq;
import com.shop.fashion.dtos.dtosReq.CategoryReqDTO;
import com.shop.fashion.dtos.dtosReq.SubCategoryToCategoryReq;
import com.shop.fashion.dtos.dtosReq.UpdateSizeDTO;
import com.shop.fashion.dtos.dtosRes.ApiMetaRes;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.CategoryDTO;
import com.shop.fashion.dtos.dtosRes.CategoryDetail;
import com.shop.fashion.dtos.dtosRes.CategoryTable;
import com.shop.fashion.dtos.dtosRes.MetadataDTO;
import com.shop.fashion.dtos.dtosRes.SizeDTO;
import com.shop.fashion.dtos.dtosRes.SubCategoryDTO;
import com.shop.fashion.services.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/category")
public class CategoryController {
        private final CategoryService categoryService;

        @Cacheable(value = "getAllCategories")
        @GetMapping("/all")
        public ApiRes<List<CategoryDTO>> getAllCategories() {
                return ApiRes.<List<CategoryDTO>>builder().code(1003).message("success")
                                .result(categoryService.getAllCategories())
                                .build();
        }

        @CacheEvict(value = "getAllCategories", allEntries = true)
        @PostMapping("/add")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiRes<CategoryTable> addCategory(@Valid @RequestBody CategoryReqDTO category) {
                return ApiRes.<CategoryTable>builder().code(1003).message("add category success")
                                .result(categoryService.addCategory(category)).build();
        }

        @PostMapping("/addSize")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiRes<SizeDTO> addSizeForCategory(@Valid @RequestBody AddSizeToCategoryReq categorySize) {
                return ApiRes.<SizeDTO>builder().code(1003).message("add size to category success")
                                .result(categoryService.addSizeToCategory(categorySize))
                                .build();
        }

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/updateSize")
        public ApiRes<SizeDTO> updateSizesToCategory(@RequestBody UpdateSizeDTO sizeDTO) {
                return ApiRes.<SizeDTO>builder().code(1003).message("update size for category success")
                                .result(categoryService.updateSizesToCategory(sizeDTO))
                                .build();
        }

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/admin/all")
        public ApiMetaRes<List<CategoryTable>> getAllCategoryForAdmin(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
                Page<CategoryTable> categoriesPage = categoryService.getAllCategoriesForAdmin(page, size);
                MetadataDTO metadata = new MetadataDTO(
                                categoriesPage.getTotalElements(),
                                categoriesPage.getTotalPages(),
                                categoriesPage.getNumber(),
                                categoriesPage.getSize());
                return ApiMetaRes.<List<CategoryTable>>builder().result(categoriesPage.getContent())
                                .message("get categories susccess")
                                .metadata(metadata)
                                .build();
        }

        @PostMapping("/addSubCategory")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiRes<SubCategoryDTO> addSubCategoryForCategory(
                        @Valid @RequestBody SubCategoryToCategoryReq subCategory) {
                return ApiRes.<SubCategoryDTO>builder().code(1003).message("add SubCategory to category success")
                                .result(categoryService.addSubCategoryForCategory(subCategory))
                                .build();
        }

        @CacheEvict(value = "getAllCategories", allEntries = true)
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/updateSubCategory")
        public ApiRes<String> updateSubCategoryForCategory(@RequestBody SubCategoryToCategoryReq subCategory) {
                categoryService.updateSubCategoryForCategory(subCategory);
                return ApiRes.<String>builder().code(1003).message("add sizes to category success")
                                .build();
        }

        @CacheEvict(value = "getAllCategories", allEntries = true)
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiRes<Void> deleteCategory(@PathVariable Long id) {
                return ApiRes.<Void>builder().code(1003).message("delete cactegory is success")
                                .result(categoryService.deleteCategory(id))
                                .build();
        }

        @DeleteMapping("/size/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiRes<Void> deleteSize(@PathVariable Long id) {
                return ApiRes.<Void>builder().code(1003).message("delete cactegory is success")
                                .result(categoryService.deleteSize(id))
                                .build();
        }

        @CacheEvict(value = "getAllCategories", allEntries = true)
        @DeleteMapping("/subCategory/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiRes<Void> deleteSubCategory(@PathVariable Long id) {
                return ApiRes.<Void>builder().code(1003).message("delete cactegory is success")
                                .result(categoryService.deleteSubCategory(id))
                                .build();
        }

        @GetMapping("/detail/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiRes<CategoryDetail> getDetailCategory(@PathVariable Long id) {
                return ApiRes.<CategoryDetail>builder().code(1003).message("delete cactegory is success")
                                .result(categoryService.getDetailCategory(id))
                                .build();
        }
}