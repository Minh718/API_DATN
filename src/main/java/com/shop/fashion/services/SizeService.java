package com.shop.fashion.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosRes.ProductSizeAdmin;
import com.shop.fashion.repositories.SizeRepository;

import lombok.RequiredArgsConstructor;

/**
 * SizeService
 */
@Service
@RequiredArgsConstructor
public class SizeService {

    private final SizeRepository sizeRepository;
    private final RedisService redisService;

    public List<ProductSizeAdmin> getAllSizeOfProductForAdmin(Long id) {
        List<ProductSizeAdmin> sizes = sizeRepository.findAllSizesOfProduct(id);
        sizes.forEach(size -> {
            var totalSales = redisService.getKey("productSize:" + size.getId());
            if (totalSales != null) {
                size.setTotalQuantity((int) totalSales);
            }
        });
        return sizes;
    }
    // // @PreAuthorize("hasRole('ADMIN')")
    // public void deleteSizesByIdCategory(Long categoryId) {
    // sizeRepository.deleteAllByCategoryId(categoryId);
    // }

}