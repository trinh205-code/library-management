package com.slayer.backend.controller;

import com.slayer.backend.payload.reponse.DashboardMetricsResponse;
import com.slayer.backend.service.impl.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/revenue")
    public ResponseEntity<DashboardMetricsResponse> getRevenueMetrics(
            @RequestParam(defaultValue = "month") String timeFrame) {
        DashboardMetricsResponse response = dashboardService.getDashboardMetrics(timeFrame);
        return ResponseEntity.ok(response);
    }
}
