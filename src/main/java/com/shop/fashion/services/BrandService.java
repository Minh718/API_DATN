package com.shop.fashion.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.ColorReq;
import com.shop.fashion.dtos.dtosRes.ColorRes;
import com.shop.fashion.dtos.dtosRes.ProductColorsAdmin;
import com.shop.fashion.entities.Brand;
import com.shop.fashion.entities.Color;
import com.shop.fashion.mappers.ColorMapper;
import com.shop.fashion.repositories.BrandRepository;
import com.shop.fashion.repositories.ColorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;

    public void addBrand(Brand brand) {
        brandRepository.save(brand);
    }

    public List<Brand> getALlBrands() {
        return brandRepository.findAll();
    }
}
