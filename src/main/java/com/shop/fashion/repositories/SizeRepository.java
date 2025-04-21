package com.shop.fashion.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Locale.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.dtos.dtosRes.ProductSizeAdmin;
import com.shop.fashion.entities.Size;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    // @Query("delete from Size s where s.category.id = ?1")
    // @Modifying
    // @Transactional
    void deleteAllByCategoryId(Long categoryId);

    Optional<Size> findByIdAndCategoryId(Long id, Long categoryId);

    Set<Size> findAllByCategoryId(Long categoryId);

    Optional<Size> findByIdAndCategory(Long id, Category category);

    @Query("SELECT new com.shop.fashion.dtos.dtosRes.ProductSizeAdmin(ps.id, s.name, COALESCE(SUM(op.quantity), 0) as totalSales) "
            +
            "FROM Size s " +
            "LEFT JOIN s.productSizes ps " +
            "LEFT JOIN ps.productSizeColors psc " +
            "LEFT JOIN psc.orderProducts op " +
            "ON op.order.orderStatus != com.shop.fashion.enums.OrderStatus.CANCELED " +
            "WHERE ps.product.id = :id " +
            "GROUP BY ps.id, s.name ORDER BY ps.id asc")
    List<ProductSizeAdmin> findAllSizesOfProduct(Long id);

}