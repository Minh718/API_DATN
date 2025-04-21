package com.shop.fashion.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.AddSizeToCategoryReq;
import com.shop.fashion.dtos.dtosReq.CategoryReqDTO;
import com.shop.fashion.dtos.dtosReq.SubCategoryToCategoryReq;
import com.shop.fashion.dtos.dtosReq.UpdateSizeDTO;
import com.shop.fashion.dtos.dtosRes.CategoryDTO;
import com.shop.fashion.dtos.dtosRes.CategoryDetail;
import com.shop.fashion.dtos.dtosRes.CategoryTable;
import com.shop.fashion.dtos.dtosRes.SizeDTO;
import com.shop.fashion.dtos.dtosRes.SubCategoryDTO;
import com.shop.fashion.dtos.dtosRes.UserResDTO;
import com.shop.fashion.entities.Category;
import com.shop.fashion.entities.Size;
import com.shop.fashion.entities.SubCategory;
import com.shop.fashion.entities.User;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.CategoryMapper;
import com.shop.fashion.mappers.UserMapper;
import com.shop.fashion.repositories.CategoryRepository;
import com.shop.fashion.repositories.SizeRepository;
import com.shop.fashion.repositories.SubCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;
    private final SubCategoryRepository subCategoryRepository;

    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = CategoryMapper.INSTANCE.toListCategoryDTO(categories);
        return categoryDTOs;
    }

    public CategoryTable addCategory(CategoryReqDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        categoryRepository.save(category);
        return CategoryMapper.INSTANCE.toCategoryTable(category);
    }

    public SizeDTO addSizeToCategory(AddSizeToCategoryReq categorySize) {
        Category category = categoryRepository.findById(categorySize.getIdCategory())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_EXISTED));
        Size size = new Size();
        size.setName(categorySize.getName());
        size.setCategory(category);
        sizeRepository.save(size);
        return SizeDTO.builder().id(size.getId()).name(size.getName()).build();
    }

    public Void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_EXISTED));
        categoryRepository.delete(category);
        return null;
    }

    public Void deleteSize(Long id) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        sizeRepository.delete(size);
        return null;
    }

    public Void deleteSubCategory(Long id) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        subCategoryRepository.delete(subCategory);
        return null;
    }

    public SizeDTO updateSizesToCategory(UpdateSizeDTO sizeDTO) {
        Size size = sizeRepository.findById(sizeDTO.getIdSize())
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        size.setName(sizeDTO.getName());
        sizeRepository.save(size);
        return SizeDTO.builder().id(size.getId()).name(sizeDTO.getName()).build();
    }

    public SubCategoryDTO addSubCategoryForCategory(SubCategoryToCategoryReq subCategoryDTO) {
        Category category = categoryRepository.findById(subCategoryDTO.getIdCategory())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_EXISTED));
        SubCategory subCategory = new SubCategory();
        subCategory.setName(subCategoryDTO.getName());
        subCategory.setThump(toSlug(subCategoryDTO.getName()));
        subCategory.setCategory(category);
        subCategoryRepository.save(subCategory);
        return SubCategoryDTO.builder().id(subCategory.getId()).name(subCategory.getName()).build();

    }

    public void updateSubCategoryForCategory(SubCategoryToCategoryReq subCategoryDTO) {
        SubCategory subCategory = subCategoryRepository.findById(subCategoryDTO.getIdCategory())
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        subCategory.setName(subCategoryDTO.getName());
        subCategory.setThump(toSlug(subCategoryDTO.getName()));
        subCategoryRepository.save(subCategory);
    }

    public Page<CategoryTable> getAllCategoriesForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categories = categoryRepository.findAll(pageable);
        Page<CategoryTable> voucherResDTOs = categories.map(CategoryMapper.INSTANCE::toCategoryTable);
        return voucherResDTOs;
    }

    public CategoryDetail getDetailCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_EXISTED));
        return CategoryMapper.INSTANCE.toCategoryDetail(category);
    }

    private String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return input.trim()
                .toLowerCase()
                .replaceAll("\\s+", "-"); // Replace one or more spaces with a hyphen
    }
}
