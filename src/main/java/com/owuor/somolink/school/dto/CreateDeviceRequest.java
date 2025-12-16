package com.owuor.somolink.school.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDeviceRequest {

    @NotBlank
    private String deviceName;

    @NotBlank
    private String macAddress;

}
