package com.owuor.somolink.payment.dto;


import com.owuor.somolink.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class paymentTransactionResponseDto {

    private Long id;
    private String profileName;
    private String phoneNumber;
    private BigDecimal amount;
    private String mpesaReceiptNumber;
    private PaymentStatus status;
    private LocalDateTime paidAt;
}
