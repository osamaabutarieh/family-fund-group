package com.familyfund.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DashboardData {
    private BigDecimal totalBalance;
    private int activeMembers;
    private BigDecimal monthlyAverage;
    private int unpaidCount;
    private MonthlyChartData monthlyData;
    private int[] transactionDistribution;

    @Data
    public static class MonthlyChartData {
        private String[] labels;
        private BigDecimal[] income;
        private BigDecimal[] expenses;
    }

    @Data
    public static class TopContributor {
        private String name;
        private String phone;
        private BigDecimal totalContribution;
        private LocalDateTime lastPaymentDate;
        private String status;
    }
} 