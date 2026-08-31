package com.slayer.backend.payload.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricsResponse {
    private Long totalRevenue;
    private Long subscriptionRevenue;
    private Long fineRevenue;
    
    private Long activeSubscriptions;
    private String topSellingPlan;
    
    private List<PeriodicRevenue> chartData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeriodicRevenue {
        private String label; 
        private Long revenue;
    }
}
