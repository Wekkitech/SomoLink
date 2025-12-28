package com.owuor.somolink.payment.dto;

import com.owuor.somolink.payment.enums.PaymentStatus;
import com.owuor.somolink.payment.enums.ProvisioningStatus;
import lombok.Data;

@Data
public class PaymentStatusResponse {

    private Long intentId;
    private PaymentStatus paymentStatus;
    private ProvisioningStatus provisioningStatus;
    private String message; // NEW: status message for user
    private Boolean callbackReceived    ;


    // Only filled when READY
    private String loginUrl;
    private String username;
    private String password;
}
