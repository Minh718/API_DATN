package com.shop.fashion.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.search.engine.search.sort.dsl.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.shop.fashion.dtos.dtosReq.ProductAddDTO;
import com.shop.fashion.dtos.dtosRes.ColorQuantityRes;
import com.shop.fashion.dtos.dtosRes.ProductDTO;
import com.shop.fashion.dtos.dtosRes.ProductDetailDTO;
import com.shop.fashion.dtos.dtosRes.ProductSizeQuantity;
import com.shop.fashion.dtos.dtosRes.ProductsHomePage;
import com.shop.fashion.entities.Brand;
import com.shop.fashion.entities.Category;
import com.shop.fashion.entities.DetailProduct;
import com.shop.fashion.entities.Product;
import com.shop.fashion.entities.ProductSize;
import com.shop.fashion.entities.RecommendProduct;
import com.shop.fashion.entities.Size;
import com.shop.fashion.entities.SubCategory;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.ProductMapper;
import com.shop.fashion.repositories.BrandRepository;
import com.shop.fashion.repositories.CategoryRepository;
import com.shop.fashion.repositories.ProductRepository;
import com.shop.fashion.repositories.RecommendProductRepository;
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
    private final RecommendProductRepository recommendProductRepository;
    private final RedisService redisService;
    private final CategoryRepository categoryRepository;

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

    public Page<ProductDTO> getDraftProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAllByStatusOrderByCreatedDate(true,
                pageable);
        Page<ProductDTO> productDTOs = products.map(ProductMapper.INSTANCE::toProductDTO);
        return productDTOs;
    }

    public Page<ProductDTO> getPublicProductsBySubCategory(int page, int size, String thump, String sortBy,
            String orderBy, Double minPrice, Double maxPrice) {
        Page<Product> products;
        Sort.Direction sortDirection = orderBy.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        SubCategory subCategory = subCategoryRepository.findByThump(thump)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBCATEGORY_NOT_EXISTED));
        if (minPrice == null) {
            products = productRepository.findAllByStatusAndSubCategory(true,
                    subCategory, pageable);
        } else {
            products = productRepository.findAllByStatusAndSubCategoryAndPriceBetween(
                    true, subCategory, minPrice, maxPrice, pageable);
        }
        return products.map(ProductMapper.INSTANCE::toProductDTO);
    }

    public Page<ProductDTO> getPublicProducts(int page, int size, String sortBy,
            String orderBy, Double minPrice, Double maxPrice) {
        Page<Product> products;
        Sort.Direction sortDirection = orderBy.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        if (minPrice == null) {
            products = productRepository.findAllByStatus(true, pageable);
        } else {
            products = productRepository.findAllByStatusAndPriceBetween(
                    true, minPrice, maxPrice, pageable);
        }
        return products.map(ProductMapper.INSTANCE::toProductDTO);
    }

    public ProductDetailDTO getProductDetail(Long id) {
        // Product product = productRepository.findById(id)
        // .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_EXISTED));
        Product product = productRepository.findByIdAndFetchProductSizesAndFetchDetailProduct(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_EXISTED));

        // will be removed
        if (product.getDetailProduct().getImages().size() == 0) {
            List<String> images = new LinkedList<>();
            images.add("https://picsum.photos/201/300");
            images.add("https://picsum.photos/202/300");
            images.add("https://picsum.photos/203/300");
            product.getDetailProduct().setImages(images);
            productRepository.save(product);
        }
        ProductDetailDTO productDetailDTO = ProductMapper.INSTANCE.toProductDetailDTO(product);
        productDetailDTO.getProductSizes().forEach(productSize -> {
            var quantityPZ = redisService.getKey("productSize:" + productSize.getId());
            if (quantityPZ == null) {
                // will be removed
                quantityPZ = processing(productSize);
                redisService.setKey("productSize:" + productSize.getId(), quantityPZ);
            }
            productSize.setQuantity((int) quantityPZ);
            if ((int) quantityPZ != 0) {
                productSize.getProductSizeColors().forEach(productSizeColor -> {

                    var quantityPSC = redisService.getKey("productSizeColor:" +
                            productSizeColor.getId());
                    if (quantityPSC == null) {
                        quantityPSC = 0;
                    }
                    productSizeColor.setQuantity((int) quantityPSC);
                });
            }
        });
        return productDetailDTO;
    }

    public int processing(ProductSizeQuantity productSize) {
        int count = (int) (Math.random() * 4);
        if (count % 4 == 0) {
            redisService.setKey("productSize:" + productSize.getId(), 0);
            count++;
            return 0;
        }
        int totalQuantity = 0;
        for (ColorQuantityRes productSizeColor : productSize.getProductSizeColors()) {
            int quantity = (int) (Math.random() * 80);
            totalQuantity += quantity;
            redisService.setKey("productSizeColor:" + productSizeColor.getId(), quantity);
        }
        return totalQuantity;
    }

    public Page<ProductDTO> searchPublicProductsByName(@NotBlank(message = "query is required") String name, int size,
            int page, String sortBy,
            String orderBy) {
        Pageable pageable = PageRequest.of(page, size);
        SortOrder sortOrder = orderBy.equals("asc") ? SortOrder.ASC : SortOrder.DESC;
        Page<Product> products = productRepository.searchPublicProduct(name, pageable, List.of("name"), sortBy,
                sortOrder);
        Page<ProductDTO> productDTOs = products.map(ProductMapper.INSTANCE::toProductDTO);
        return productDTOs;
    }

    public List<ProductDTO> getRelatedProducts(Long id) {
        Pageable top8 = PageRequest.of(0, 8);
        List<RecommendProduct> recommendProducts = recommendProductRepository.findByProduct(id, top8);

        if (recommendProducts.isEmpty()) {
            return ProductMapper.INSTANCE.toProductDTOs(
                    productRepository.findAllByStatusOrderByCreatedDate(true, top8).getContent());
        }

        List<Product> products = recommendProducts.stream()
                .map(rp -> (rp.getP1().equals(id) ? rp.getP2() : rp.getP1())) // Get related product ID
                .map(idRelated -> productRepository.findByIdAndStatus(idRelated, true)) // Fetch product
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return ProductMapper.INSTANCE.toProductDTOs(products);
    }

    public List<ProductsHomePage> getListProductsForHomePage() {
        List<Category> categories = categoryRepository.findAll();
        List<ProductsHomePage> listProductsHomePage = new LinkedList<>();
        for (Category category : categories) {
            List<ProductDTO> products = getPublicNewestProductsByCategory(10,
                    category.getId());
            listProductsHomePage.add(new ProductsHomePage(category.getName(), products));
        }
        return listProductsHomePage;
    }

    public List<ProductDTO> getPublicNewestProductsByCategory(int size,
            Long idCategory) {
        Pageable pageable = PageRequest.of(0, size);
        Page<Product> products = productRepository.findAllByStatusAndCategoryIdOrderByCreatedDateDesc(true,
                idCategory, pageable);
        Page<ProductDTO> productDTOs = products.map(ProductMapper.INSTANCE::toProductDTO);
        return productDTOs.getContent();
    }
}