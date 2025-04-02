package com.shop.fashion.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.dtos.dtosRes.StatisticOrderDTO;
import com.shop.fashion.entities.Order;
import com.shop.fashion.enums.OrderStatus;

import feign.Param;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
        @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
        Page<Order> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

        @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderStatus = :orderStatus ORDER BY o.createdAt DESC")
        Page<Order> findAllByUserIdAndOrderStatusOrderByCreatedAtDesc(String userId, OrderStatus orderStatus,
                        Pageable pageable);

        @Query("SELECT o FROM Order o JOIN FETCH o.orderProducts op JOIN FETCH op.productSizeColor psc JOIN FETCH psc.productSize WHERE o.id = :id ")
        Optional<Order> findByIdFetchOrderProductFetchProductSizeColorFetchProductSize(Long id);

        @Query("SELECT new  com.shop.fashion.dtos.dtosRes.StatisticOrderDTO(" +
                        "COALESCE(COUNT(o), 0), " +
                        "COALESCE(SUM(o.totalAmount), 0), " +
                        "COALESCE(SUM(op.quantity), 0)) " +
                        "FROM Order o JOIN o.orderProducts op " +
                        "WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.orderStatus <> :canceledStatus")
        StatisticOrderDTO findStatisticOrderByCreatedAtBetween(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("canceledStatus") OrderStatus canceledStatus);

        @Query(nativeQuery = true, value = "SELECT \n" +
                        " days.date AS date,\n" +
                        " COALESCE(SUM(o.total_amount), 0) AS revenue\n" +
                        "FROM \n" +
                        " (SELECT CURRENT_DATE - INTERVAL '1 day' * n AS date\n" +
                        " FROM generate_series(0, 6) AS n) AS days\n"
                        +
                        "LEFT JOIN \n" +
                        " \"order\" o ON DATE(o.created_at) = days.date \n" +
                        " AND o.order_status != 'CANCELED'\n" +
                        "GROUP BY \n" +
                        " days.date\n" +
                        "ORDER BY \n" +
                        " days.date;")

        List<Object[]> statisticRevennue7LastDays();

        @Query(nativeQuery = true, value = "SELECT \n" +
                        " months.month AS month,\n" +
                        " COALESCE(SUM(o.total_amount), 0) AS revenue\n" +
                        "FROM \n" +
                        " (SELECT TO_CHAR(DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month' * n, 'YYYY-MM') AS month\n"
                        +
                        " FROM generate_series(0, 6) AS n) AS months\n" +
                        "LEFT JOIN \n" +
                        " \"order\" o ON TO_CHAR(o.created_at, 'YYYY-MM') = months.month \n" +
                        " AND o.created_at >= DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '6 months' \n" +
                        " AND o.order_status != 'CANCELED'\n" +
                        "GROUP BY \n" +
                        " months.month\n" +
                        "ORDER BY \n" +
                        " months.month;")
        List<Object[]> statisticRevennue7LastMonths();

        @Query(nativeQuery = true, value = "SELECT \n" +
                        " weeks.week AS week,\n" +
                        " COALESCE(SUM(o.total_amount), 0) AS revenue\n" +
                        "FROM \n" +
                        " (SELECT EXTRACT(WEEK FROM CURRENT_DATE - INTERVAL '1 week' * n) AS week\n" +
                        " FROM generate_series(0, 6) AS n) AS weeks\n"
                        +
                        "LEFT JOIN \n" +
                        " \"order\" o ON weeks.week = EXTRACT(WEEK FROM o.created_at) \n" +
                        " AND o.created_at >= CURRENT_DATE - INTERVAL '7 weeks' \n" +
                        " AND o.order_status != 'CANCELED'\n" +
                        "GROUP BY \n" +
                        " weeks.week\n" +
                        "ORDER BY \n" +
                        " weeks.week;")

        List<Object[]> statisticRevennue7LastWeeks();

        @Query(nativeQuery = true, value = "SELECT \n" +
                        " years.year AS year,\n" +
                        " COALESCE(SUM(o.total_amount), 0) AS revenue\n" +
                        "FROM \n" +
                        " (SELECT EXTRACT(YEAR FROM CURRENT_DATE) - n AS year\n" +
                        " FROM generate_series(0, 6) AS n) AS years\n" +
                        "LEFT JOIN \n" +
                        " \"order\" o ON EXTRACT(YEAR FROM o.created_at) = years.year \n" +
                        " AND o.created_at >= CURRENT_DATE - INTERVAL '7 years' \n" +
                        " AND o.order_status != 'CANCELED'\n" +
                        "GROUP BY \n" +
                        " years.year\n" +
                        "ORDER BY \n" +
                        " years.year;")

        List<Object[]> statisticRevennue7LastYears();
}