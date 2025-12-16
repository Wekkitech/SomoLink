package com.owuor.somolink.network.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserProfileRequest {

    @NotBlank
    private String profileName;           // e.g., "School12_Profile"

    @PositiveOrZero
    private int rateLimitUpload;          // kbps

    @PositiveOrZero
    private int rateLimitDownload;        // kbps

    private String sessionTimeout;        // optional, e.g., "1h"

    private String idleTimeout;           // optional, e.g., "10m"

    private BigDecimal amount;
}
