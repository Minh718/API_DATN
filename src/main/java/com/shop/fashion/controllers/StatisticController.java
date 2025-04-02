package com.shop.fashion.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.StatisticsDTO;
import com.shop.fashion.enums.TypeStatistic;
import com.shop.fashion.services.StatisticService;

import lombok.RequiredArgsConstructor;

/**
 * StatisticController
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/statistic")
public class StatisticController {
    private final StatisticService statisticService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ApiRes<StatisticsDTO> getStatisticsDashboard(@RequestParam(defaultValue = "DAY") TypeStatistic type) {
        return ApiRes.<StatisticsDTO>builder()
                .result(statisticService.getStatisticsDashboard(type))
                .build();
    }

}