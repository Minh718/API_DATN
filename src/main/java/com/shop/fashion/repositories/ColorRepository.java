package com.shop.fashion.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.dtos.dtosRes.ProductColorsAdmin;
import com.shop.fashion.entities.Color;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {

        @Query(nativeQuery = true, value = "SELECT * \n" + //
                        "FROM color \n" + //
                        "WHERE id NOT IN (\n" + //
                        "    SELECT psc.color_id \n" + //
                        "    FROM product_size_color psc \n" + //
                        "    WHERE product_size_id = :idProductSize \n" + //
                        "    AND psc.color_id IS NOT NULL\n" + //
                        ");")
        List<Color> findAllColorNotInProductSize(Long idProductSize);

        @Query("SELECT new com.shop.fashion.dtos.dtosRes.ProductColorsAdmin(psc.id, c.name, c.code, COALESCE(SUM(op.quantity), 0) as totalSales) "
                        +
                        "FROM Color c " +
                        "LEFT JOIN c.productSizeColors psc " +
                        "LEFT JOIN psc.orderProducts op " +
                        "ON op.order.orderStatus IS NULL OR op.order.orderStatus != com.shop.fashion.enums.OrderStatus.CANCELED "
                        +
                        "WHERE psc.productSize.id = :id " +
                        "GROUP BY psc.id, c.id, c.name, c.code")
        List<ProductColorsAdmin> findAllColorsOfProduct(Long id);

}
