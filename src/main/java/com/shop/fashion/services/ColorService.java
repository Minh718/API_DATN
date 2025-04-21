package com.shop.fashion.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.ColorReq;
import com.shop.fashion.dtos.dtosRes.CategoryTable;
import com.shop.fashion.dtos.dtosRes.ColorRes;
import com.shop.fashion.dtos.dtosRes.ColorTable;
import com.shop.fashion.dtos.dtosRes.ProductColorsAdmin;
import com.shop.fashion.entities.Category;
import com.shop.fashion.entities.Color;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.CategoryMapper;
import com.shop.fashion.mappers.ColorMapper;
import com.shop.fashion.repositories.ColorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ColorService {
    private final ColorRepository colorRepository;
    private final RedisService redisService;

    public ColorTable addColor(ColorReq colorDTO) {
        Color color = colorRepository.save(ColorMapper.INSTANCE.toColor(colorDTO));
        return ColorMapper.INSTANCE.toColorTable(color);

    }

    public Void deleteColor(Long id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.COLOR_NOT_EXISTED));
        colorRepository.delete(color);
        return null;
    }

    public List<ColorRes> findAllColorNotInProductSize(Long idProductSize) {
        List<Color> colors = colorRepository.findAllColorNotInProductSize(idProductSize);
        return ColorMapper.INSTANCE.toColorRess(colors);
    }

    public Page<ColorTable> getAllColorsForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Color> colorss = colorRepository.findAll(pageable);
        Page<ColorTable> colorDTOs = colorss.map(ColorMapper.INSTANCE::toColorTable);
        return colorDTOs;
    }

    public List<ProductColorsAdmin> getAllColorsOfProduct(Long id) {
        List<ProductColorsAdmin> colors = colorRepository.findAllColorsOfProduct(id);
        colors.forEach(color -> {
            var totalQuantity = redisService.getKey("productSizeColor:" + color.getId());
            if (totalQuantity != null) {
                color.setTotalQuantity((int) totalQuantity);
            }
        });
        return colors;
    }
}
