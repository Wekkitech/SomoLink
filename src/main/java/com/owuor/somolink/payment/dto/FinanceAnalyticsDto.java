package com.owuor.somolink.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FinanceAnalyticsDto {
    private BigDecimal totalRevenue;
    private BigDecimal todaysRevenue;
    private long totalTransactions;
    private long successfulTransactions;
    private long failedTransactions;
}
