package com.owuor.somolink.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentInitiateRequest {

    @NotBlank
    private String phoneNumber;
    @NotNull
    private Long profileId;
    @NotNull
    private String macAddress;
    @NotNull
    private String profileName;
}
