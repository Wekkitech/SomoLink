package com.owuor.somolink.payment.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVerificationResponse {

    private boolean paid;
    private boolean userCreated;

    private String profileName;
    private BigDecimal amount;
    private LocalDateTime paidAt;

    // Only if hotspot user exists
    private String username;
    private String password;
    private String loginUrl;
}
