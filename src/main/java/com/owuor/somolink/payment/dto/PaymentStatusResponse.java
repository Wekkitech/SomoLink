package com.owuor.somolink.payment.dto;

import com.owuor.somolink.payment.enums.PaymentStatus;
import lombok.Data;

@Data
public class PaymentStatusResponse {

    private Long intentId;
    private PaymentStatus status;

    // Only filled when READY
    private String loginUrl;
    private String username;
    private String password;
}
