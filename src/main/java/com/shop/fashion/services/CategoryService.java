package com.shop.fashion.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosRes.CategoryDTO;
import com.shop.fashion.entities.Category;
import com.shop.fashion.mappers.CategoryMapper;
import com.shop.fashion.repositories.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = CategoryMapper.INSTANCE.toListCategoryDTO(categories);
        return categoryDTOs;
    }

}
