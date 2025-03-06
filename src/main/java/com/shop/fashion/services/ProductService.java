package com.shop.fashion.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.search.engine.search.sort.dsl.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.shop.fashion.dtos.dtosReq.ProductAddDTO;
import com.shop.fashion.dtos.dtosRes.ProductDTO;
import com.shop.fashion.entities.Brand;
import com.shop.fashion.entities.Category;
import com.shop.fashion.entities.DetailProduct;
import com.shop.fashion.entities.Product;
import com.shop.fashion.entities.ProductSize;
import com.shop.fashion.entities.Size;
import com.shop.fashion.entities.SubCategory;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.ProductMapper;
import com.shop.fashion.repositories.BrandRepository;
import com.shop.fashion.repositories.ProductRepository;
import com.shop.fashion.repositories.SubCategoryRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final SubCategoryRepository subCategoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    @NonFinal
    private final String uploadDir = "uploads/";

    public ProductDTO addProduct(ProductAddDTO productAddDTO) {
        SubCategory subCategory = subCategoryRepository.findByIdWithCategory(productAddDTO.subCate_id())
                .orElseThrow(() -> new CustomException(ErrorCode.SUBCATEGORY_NOT_EXISTED));
        Category category = subCategory.getCategory();

        Brand brand = brandRepository.findById(productAddDTO.brand_id())
                .orElseThrow(() -> new CustomException(ErrorCode.BRAND_NOT_EXISTED));

        List<ProductSize> productSizes = new LinkedList<>();
        for (Size size : category.getSizes()) {
            ProductSize productSize = new ProductSize();
            productSize.setSize(size);
            productSizes.add(productSize);
        }
        String nameImage = saveImage(productAddDTO.file());

        List<String> images = new LinkedList<>();
        for (MultipartFile file : productAddDTO.files()) {
            images.add(saveImage(file));
        }

        DetailProduct detailProduct = DetailProduct.builder()
                .description(productAddDTO.description())
                .model(productAddDTO.model())
                .material(productAddDTO.material())
                .origin(productAddDTO.origin())
                .warranty(productAddDTO.warranty())
                .madeIn(productAddDTO.madeIn())
                .images(images)
                .build();
        Product product = Product.builder()
                .name(productAddDTO.name())
                .price(productAddDTO.price())
                .percent(productAddDTO.percent())
                .categoryId(category.getId())
                .status(productAddDTO.status())
                .subCategory(subCategory)
                .brand(brand)
                .image(nameImage)
                .detailProduct(detailProduct)
                .productSizes(productSizes)
                .build();
        // Save product again to update the list of product sizes
        productRepository.save(product);
        return ProductMapper.INSTANCE.toProductDTO(product);
    }

    private String saveImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Image file is required");
            }
            validateFile(file);
            // Create unique image name
            String imageName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + imageName);

            // Create directories if they do not exist
            Files.createDirectories(path.getParent());

            // Save the file to the directory
            Files.write(path, file.getBytes());

            return imageName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store image file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        String fileType = file.getContentType();

        if (!fileType.equals("image/png") && !fileType.equals("image/jpeg")) {
            throw new RuntimeException("Invalid file type. Only PNG and JPEG are allowed.");
        }
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_EXISTED));
        productRepository.delete(product);
    }
}