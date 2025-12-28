package com.owuor.somolink.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedPaymentResponse {

    private long total;
    private List<paymentTransactionResponseDto> transactions;
}
