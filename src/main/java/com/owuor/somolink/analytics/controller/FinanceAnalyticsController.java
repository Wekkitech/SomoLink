package com.owuor.somolink.analytics.controller;

import com.owuor.somolink.payment.dto.FinanceAnalyticsDto;
import com.owuor.somolink.payment.dto.RevenueGraphDto;
import com.owuor.somolink.payment.service.FinanceAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/analytics" )
@RequiredArgsConstructor
public class FinanceAnalyticsController {

    private final FinanceAnalyticsService service;

    @GetMapping("/finance")
    public FinanceAnalyticsDto getAnalytics() {
        return service.getAnalytics();
    }


    @GetMapping("/yearly")
    public ResponseEntity<List<RevenueGraphDto>> yearly(@RequestParam int year) {
        return ResponseEntity.ok(service.getYearlyRevenue(year));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<RevenueGraphDto>> monthly(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(service.getMonthlyRevenue(year, month));
    }
}
