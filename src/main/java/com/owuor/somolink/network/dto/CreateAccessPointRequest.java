package com.owuor.somolink.network.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccessPointRequest {

    @NotBlank(message = "AP name is required")
    private String apName;

    private boolean bridgeMode;

    private String macAddress;     // optional
    private String model;          // optional
    private String schoolName;     // optional
    private String ipAddress;      // optional, for management
    private String description;    // optional
}
