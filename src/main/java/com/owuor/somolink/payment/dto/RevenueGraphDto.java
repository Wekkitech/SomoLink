package com.owuor.somolink.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RevenueGraphDto {
    private String label; // Month name or day
    private BigDecimal revenue;
}
