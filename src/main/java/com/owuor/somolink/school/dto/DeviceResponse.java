package com.owuor.somolink.school.dto;

import com.owuor.somolink.school.entity.School;
import lombok.Data;

@Data
public class DeviceResponse {

    private Long id;
    private String deviceName;
    private String macAddress;
    private School school;
}
