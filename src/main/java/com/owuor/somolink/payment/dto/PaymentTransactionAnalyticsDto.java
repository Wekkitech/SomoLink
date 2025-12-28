package com.owuor.somolink.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentTransactionAnalyticsDto {
    private BigDecimal sumAmount;
}
