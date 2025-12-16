package com.owuor.somolink.network.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SchoolDeviceLoginRequest {

    @NotBlank
    private String mac;

    @NotBlank
    private String schoolCode;
}
