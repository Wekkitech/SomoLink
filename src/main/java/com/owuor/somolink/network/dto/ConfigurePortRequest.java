package com.owuor.somolink.network.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class ConfigurePortRequest {

    @NotBlank(message = "Port name is required")
    private String portName;      // e.g. "ether2"

    @NotBlank(message = "IP Address is required")
    @Pattern(
            regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$",
            message = "Invalid IP format (expected: 192.168.10.1)"
    )
    private String ipAddress;     // e.g. "192.168.50.1"

    @Min(value = 16, message = "Subnet mask must be >= 16")
    @Max(value = 30, message = "Subnet mask must be <= 30")
    private int subnetMask;       // e.g. 24

    private String description;   // Optional label like "School 103"
}
