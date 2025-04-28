package com.shop.fashion.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.shop.fashion.dtos.dtosReq.ProductAddDTO;
import com.shop.fashion.dtos.dtosReq.ProductSizeColorQuantityDTO;
import com.shop.fashion.dtos.dtosRes.ProductDTO;
import com.shop.fashion.entities.Brand;
import com.shop.fashion.entities.Category;
import com.shop.fashion.entities.Product;
import com.shop.fashion.entities.ProductSize;
import com.shop.fashion.entities.ProductSizeColor;
import com.shop.fashion.entities.SubCategory;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.BrandRepository;
import com.shop.fashion.repositories.ProductRepository;
import com.shop.fashion.repositories.ProductSizeColorRepository;
import com.shop.fashion.repositories.SubCategoryRepository;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.utils.FileUploadUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    private SubCategoryRepository subCategoryRepository;
    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductSizeColorRepository productSizeColorRepository;

    @Mock
    private RedisService redisService;
    @Mock
    private ProductRepository productRepository;

    @Test
    void addProduct_subCategoryNotFound_shouldThrowCustomException() {
        // Arrange
        ProductAddDTO productAddDTO = new ProductAddDTO(
                "Test Product", // Name
                1000, // Price
                10, // Percent
                999L, // Invalid SubCategory ID (not in DB)
                888L, // Invalid Brand ID (not in DB)
                true, // Status
                mock(MultipartFile.class), // Image file (mocked)
                "Test Description", // Description
                "Model A", // Model
                "Material A", // Material
                "Origin A", // Origin
                "1 Year", // Warranty
                "USA", // Made In
                new ArrayList<>() // Files
        );

        // Mocking subCategoryRepository to return empty Optional (subCategory not
        // found)
        when(subCategoryRepository.findByIdWithCategory(999L)).thenReturn(Optional.empty());

        // // Mocking brandRepository to return empty Optional (brand not found)
        // when(brandRepository.findById(888L)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException thrown = assertThrows(CustomException.class, () -> {
            productService.addProduct(productAddDTO);
        });

        // Assert that the exception message is correct
        assertEquals(ErrorCode.SUBCATEGORY_NOT_EXISTED, thrown.getErrorCode());

        // Verify that the repositories were called
        verify(subCategoryRepository).findByIdWithCategory(999L);
        // verify(brandRepository).findById(888L);
    }

    @Test
    void addProduct_BrandNotFound_shouldThrowCustomException() {
        // Arrange
        ProductAddDTO productAddDTO = new ProductAddDTO(
                "Test Product", // Name
                1000, // Price
                10, // Percent
                999L, // Invalid SubCategory ID (not in DB)
                888L, // Invalid Brand ID (not in DB)
                true, // Status
                mock(MultipartFile.class), // Image file (mocked)
                "Test Description", // Description
                "Model A", // Model
                "Material A", // Material
                "Origin A", // Origin
                "1 Year", // Warranty
                "USA", // Made In
                new ArrayList<>() // Files
        );

        when(subCategoryRepository.findByIdWithCategory(999L)).thenReturn(Optional.of(new SubCategory()));

        when(brandRepository.findById(888L)).thenReturn(Optional.empty());

        CustomException thrown = assertThrows(CustomException.class, () -> {
            productService.addProduct(productAddDTO);
        });

        assertEquals(ErrorCode.BRAND_NOT_EXISTED, thrown.getErrorCode());

        verify(subCategoryRepository).findByIdWithCategory(999L);
        verify(brandRepository).findById(888L);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void addProduct_validData_shouldAddProductSuccessfully() throws IOException {
        // Arrange
        MultipartFile mockedImageFile = mock(MultipartFile.class);
        when(mockedImageFile.isEmpty()).thenReturn(false);
        when(mockedImageFile.getContentType()).thenReturn("image/png"); // Ensure it returns a valid image type
        when(mockedImageFile.getOriginalFilename()).thenReturn("test.png");
        when(mockedImageFile.getBytes()).thenReturn("dummy".getBytes());

        ProductAddDTO productAddDTO = new ProductAddDTO(
                "Test Product",
                1000,
                10,
                1L,
                1L,
                true,
                mockedImageFile,
                "Test Description",
                "Model A",
                "Material A",
                "Origin A",
                "1 Year",
                "USA",
                new ArrayList<>());

        // Mock repositories
        SubCategory subCategory = new SubCategory();
        subCategory.setId(1L);
        Category category = new Category();
        category.setId(2L);
        category.setSizes(List.of()); // prevent NPE on getSizes
        subCategory.setCategory(category);
        when(subCategoryRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(subCategory));

        Brand brand = new Brand();
        brand.setId(1L);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        // Mock static saveImage method (Mock the static method only once)
        try (MockedStatic<FileUploadUtil> mockedStatic = Mockito.mockStatic(FileUploadUtil.class)) {
            mockedStatic.when(() -> FileUploadUtil.saveImage(any(MultipartFile.class)))
                    .thenReturn("dummy.jpg");

            // Act
            ProductDTO result = productService.addProduct(productAddDTO);

            // Assert
            assertNotNull(result);
            assertEquals("Test Product", result.getName());
            assertEquals(1000, result.getPrice());
            verify(productRepository).save(any(Product.class)); // Ensure the product is saved
        } catch (Exception e) {
            fail("Static mocking failed: " + e.getMessage());
        }
    }

    @Test
    void updateQuantityForProduct_validData_shouldUpdateSuccessfully() {
        // Arrange
        Long pscId = 1L;
        int oldQuantity = 5;
        int newQuantity = 10;

        ProductSize productSize = new ProductSize();
        productSize.setId(100L);

        ProductSizeColor productSizeColor = new ProductSizeColor();
        productSizeColor.setId(pscId);
        productSizeColor.setProductSize(productSize);

        ProductSizeColorQuantityDTO dto = new ProductSizeColorQuantityDTO();
        dto.setPscId(pscId);
        dto.setQuantity(newQuantity);

        when(productSizeColorRepository.findById(pscId)).thenReturn(Optional.of(productSizeColor));
        when(redisService.getKey("productSizeColor:" + pscId)).thenReturn(oldQuantity);

        // Act
        productService.updateQuantityForProduct(dto);

        // Assert
        verify(redisService).setKey("productSizeColor:" + pscId, newQuantity);
        verify(redisService).incrementKey("productSize:" + productSize.getId(), newQuantity - oldQuantity);
    }

    @Test
    void updateQuantityForProduct_negativeQuantity_shouldThrowException() {
        // Arrange
        ProductSizeColorQuantityDTO dto = new ProductSizeColorQuantityDTO();
        dto.setPscId(1L);
        dto.setQuantity(-5); // Invalid negative quantity

        // Act & Assert
        CustomException ex = assertThrows(CustomException.class, () -> {
            productService.updateQuantityForProduct(dto);
        });

        assertEquals(ErrorCode.BAD_REQUEST, ex.getErrorCode());
    }

    @Test
    void updateQuantityForProduct_productSizeColorNotFound_shouldThrowException() {
        // Arrange
        Long pscId = 99L;
        ProductSizeColorQuantityDTO dto = new ProductSizeColorQuantityDTO();
        dto.setPscId(pscId);
        dto.setQuantity(5);

        when(productSizeColorRepository.findById(pscId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException ex = assertThrows(CustomException.class, () -> {
            productService.updateQuantityForProduct(dto);
        });

        assertEquals(ErrorCode.PRODUCT_SIZE_COLOR_NOT_EXISTED, ex.getErrorCode());
    }

    @Test
    void updateQuantityForProduct_noExistingQuantityInRedis_shouldDefaultToZero() {
        // Arrange
        Long pscId = 1L;
        int newQuantity = 7;

        ProductSize productSize = new ProductSize();
        productSize.setId(100L);

        ProductSizeColor productSizeColor = new ProductSizeColor();
        productSizeColor.setId(pscId);
        productSizeColor.setProductSize(productSize);

        ProductSizeColorQuantityDTO dto = new ProductSizeColorQuantityDTO();
        dto.setPscId(pscId);
        dto.setQuantity(newQuantity);

        when(productSizeColorRepository.findById(pscId)).thenReturn(Optional.of(productSizeColor));
        when(redisService.getKey("productSizeColor:" + pscId)).thenReturn(null); // Simulate no value in Redis

        // Act
        productService.updateQuantityForProduct(dto);

        // Assert
        verify(redisService).setKey("productSizeColor:" + pscId, newQuantity);
        verify(redisService).incrementKey("productSize:" + productSize.getId(), newQuantity); // 0 -> newQuantity
    }
}
