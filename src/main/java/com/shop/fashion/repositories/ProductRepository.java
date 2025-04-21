package com.shop.fashion.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.dtos.dtosRes.ProductTable;
import com.shop.fashion.entities.Product;
import com.shop.fashion.entities.SubCategory;

import feign.Param;

@Repository
public interface ProductRepository
                extends JpaRepository<Product, Long>, ProductSearchRepository {

        Page<Product> findAllByStatusAndSubCategory(boolean status, SubCategory subCategory, Pageable pageable);

        Page<Product> findAllByStatus(boolean status, Pageable pageable);

        @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createdDate DESC")
        Page<Product> findAllByStatusOrderByCreatedDate(@Param("status") boolean status, Pageable pageable);

        Page<Product> findAllByStatusAndPriceBetween(
                        boolean status, double minPrice, double maxPrice, Pageable pageable);

        Page<Product> findAllByStatusAndSubCategoryAndPriceBetween(
                        boolean status, SubCategory subCategory, double minPrice, double maxPrice, Pageable pageable);

        @Query("SELECT  p FROM Product p LEFT JOIN FETCH p.detailProduct  LEFT JOIN FETCH p.productSizes ps LEFT JOIN FETCH ps.productSizeColors  WHERE p.id = :id AND p.status = TRUE")
        Optional<Product> findByIdAndFetchProductSizesAndFetchDetailProduct(Long id);

        @Query("SELECT  p FROM Product p LEFT JOIN FETCH p.productSizes ps WHERE p.id = :id")
        Optional<Product> findByIdAndFetchProductSizes(Long id);

        Optional<Product> findByIdAndStatus(Long id, boolean status);

        Page<Product> findAllByStatusAndCategoryIdOrderByCreatedDateDesc(boolean status, Long categoryId,
                        Pageable pageable);

        @Query("SELECT  p FROM Product p LEFT JOIN FETCH p.detailProduct  LEFT JOIN FETCH p.subCategory LEFT JOIN FETCH p.brand  WHERE p.id = :id")
        Optional<Product> findByIdAndFetchDetailProduct(Long id);

        @Query("SELECT new com.shop.fashion.dtos.dtosRes.ProductTable(" +
                        "p.id, p.name, p.price, p.image,p.percent, p.status, " +
                        "COALESCE(SUM(op.quantity), 0), " +
                        "COALESCE(SUM(op.quantity * op.price), 0L)) " +
                        "FROM Product p " +
                        "LEFT JOIN p.productSizes ps " +
                        "LEFT JOIN ps.productSizeColors psc " +
                        "LEFT JOIN psc.orderProducts op " +
                        "WITH op.order.orderStatus != com.shop.fashion.enums.OrderStatus.CANCELED " +
                        "GROUP BY p.id, p.name, p.price, p.image, p.createdDate, p.status")
        Page<ProductTable> findAllProductForTable(Pageable pageable);
}