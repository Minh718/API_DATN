package com.shop.fashion.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosRes.StatisticCompare;
import com.shop.fashion.dtos.dtosRes.StatisticOrderDTO;
import com.shop.fashion.dtos.dtosRes.StatisticRevennueDTO;
import com.shop.fashion.dtos.dtosRes.StatisticsDTO;
import com.shop.fashion.enums.OrderStatus;
import com.shop.fashion.enums.TypeStatistic;
import com.shop.fashion.repositories.OrderRepository;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.utils.DateTimeUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticService {
        private final OrderRepository orderRepository;
        private final UserRepository userRepository;

        public StatisticsDTO getStatisticsDashboard(TypeStatistic type) {
                LocalDateTime now = DateTimeUtil.getCurrentVietnamTime();
                LocalDateTime start, prev, end;
                List<Object[]> results = null;

                // Determine time ranges and statistic method based on the type
                switch (type) {
                        case DAY:
                                start = now.minusDays(1);
                                prev = now.minusDays(2);
                                end = now;
                                results = orderRepository.statisticRevennue7LastDays();
                                break;
                        case MONTH:
                                start = now.minusMonths(1);
                                prev = now.minusMonths(2);
                                end = now;
                                results = orderRepository.statisticRevennue7LastMonths();
                                break;
                        case WEEK:
                                start = now.minusWeeks(1);
                                prev = now.minusWeeks(2);
                                end = now;
                                results = orderRepository.statisticRevennue7LastWeeks();
                                break;
                        case YEAR:
                                start = now.minusYears(1);
                                prev = now.minusYears(2);
                                end = now;
                                results = orderRepository.statisticRevennue7LastYears();
                                break;
                        default:
                                throw new RuntimeException("Type not found");
                }
                List<StatisticRevennueDTO> statisticRevennue = results.stream()
                                .map(result -> new StatisticRevennueDTO(result[0].toString(),
                                                ((Number) result[1]).longValue()))
                                .collect(Collectors.toList());
                // Retrieve statistics
                StatisticOrderDTO prevStatistic = orderRepository.findStatisticOrderByCreatedAtBetween(prev, start,
                                OrderStatus.CANCELED);
                StatisticOrderDTO currentStatistic = orderRepository.findStatisticOrderByCreatedAtBetween(start, end,
                                OrderStatus.CANCELED);

                // Calculate percentage changes with zero-division check
                double percentTotalOrders = calculatePercentageChange(prevStatistic.getTotalOrders(),
                                currentStatistic.getTotalOrders());
                double percentTotalRevenue = calculatePercentageChange(prevStatistic.getTotalRevenue(),
                                currentStatistic.getTotalRevenue());
                double percentTotalQuantity = calculatePercentageChange(prevStatistic.getTotalQuantity(),
                                currentStatistic.getTotalQuantity());

                // Retrieve new user counts
                long prevTotalUser = userRepository.countTotalNewUser(prev, start);
                long currentTotalUser = userRepository.countTotalNewUser(start, end);

                double percentNewCustomers = calculatePercentageChange(prevTotalUser,
                                currentTotalUser);

                // Create and return the DTO
                StatisticCompare<Long> totalOrders = new StatisticCompare<>(currentStatistic.getTotalOrders(),
                                percentTotalOrders);
                StatisticCompare<Double> totalRevenue = new StatisticCompare<>(currentStatistic.getTotalRevenue(),
                                percentTotalRevenue);
                StatisticCompare<Long> totalQuantity = new StatisticCompare<Long>(currentStatistic.getTotalQuantity(),
                                percentTotalQuantity);
                StatisticCompare<Long> totalNewCustomers = new StatisticCompare<>(currentTotalUser,
                                percentNewCustomers);
                return new StatisticsDTO(totalOrders, totalRevenue, totalQuantity,
                                totalNewCustomers,
                                statisticRevennue);
        }

        // Helper method to avoid division by zero
        private double calculatePercentageChange(double previous, double current) {
                if (previous == 0) {
                        return current == 0 ? 0 : 100; // If previous is 0 and current is not, return 100%
                }
                return (current - previous) / previous * 100;
        }

}
