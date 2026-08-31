package com.slayer.backend.service.impl;

import com.slayer.backend.Enums.PaymentStatus;
import com.slayer.backend.Enums.PaymentType;
import com.slayer.backend.Enums.FineStatus;
import com.slayer.backend.payload.reponse.DashboardMetricsResponse;
import com.slayer.backend.repository.PaymentRepository;
import com.slayer.backend.repository.SubscriptionRepository;
import com.slayer.backend.repository.FineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FineRepository fineRepository;

    public DashboardMetricsResponse getDashboardMetrics(String timeFrame) {
        Long totalRevenue = paymentRepository.getTotalRevenue(PaymentStatus.SUCCESS);
        Long subRevenue = paymentRepository.getRevenueByType(PaymentStatus.SUCCESS, PaymentType.MEMBERSHIP);
        Long fineRevenue = fineRepository.getTotalFinesIssued(FineStatus.WAIVED);
        if (fineRevenue == null) fineRevenue = 0L;
        
        Long activeSubs = subscriptionRepository.countActiveSubscriptions(LocalDate.now());
        List<String> topPlans = subscriptionRepository.findTopSellingPlans(PageRequest.of(0, 1));
        String topPlan = topPlans.isEmpty() ? "N/A" : topPlans.get(0);

        LocalDateTime startDate;
        List<Object[]> rawChartData;
        
        switch (timeFrame.toLowerCase()) {
            case "day":
                startDate = LocalDateTime.now().minusDays(30);
                rawChartData = paymentRepository.getDailyRevenue(PaymentStatus.SUCCESS, startDate);
                break;
            case "week":
                startDate = LocalDateTime.now().minusWeeks(12);
                rawChartData = paymentRepository.getWeeklyRevenue(PaymentStatus.SUCCESS, startDate);
                break;
            case "month":
            default:
                startDate = LocalDateTime.now().minusYears(1);
                rawChartData = paymentRepository.getMonthlyRevenue(PaymentStatus.SUCCESS, startDate);
                break;
        }
        
        List<DashboardMetricsResponse.PeriodicRevenue> chartData = new ArrayList<>();
        for (Object[] row : rawChartData) {
            Integer year = (Integer) row[0];
            String label = "";
            Long total;
            
            if (timeFrame.equalsIgnoreCase("day")) {
                Integer month = (Integer) row[1];
                Integer day = (Integer) row[2];
                total = ((Number) row[3]).longValue();
                label = String.format("%04d-%02d-%02d", year, month, day);
            } else if (timeFrame.equalsIgnoreCase("week")) {
                Integer week = (Integer) row[1];
                total = ((Number) row[2]).longValue();
                label = String.format("%04d-W%02d", year, week);
            } else {
                Integer month = (Integer) row[1];
                total = ((Number) row[2]).longValue();
                label = String.format("%04d-%02d", year, month);
            }
            
            chartData.add(new DashboardMetricsResponse.PeriodicRevenue(label, total));
        }

        return DashboardMetricsResponse.builder()
                .totalRevenue(totalRevenue)
                .subscriptionRevenue(subRevenue)
                .fineRevenue(fineRevenue)
                .activeSubscriptions(activeSubs)
                .topSellingPlan(topPlan)
                .chartData(chartData)
                .build();
    }
}
