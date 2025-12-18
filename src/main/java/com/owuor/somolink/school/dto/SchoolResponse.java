package com.owuor.somolink.school.dto;

import com.owuor.somolink.network.dto.BridgeConfigurationResponseDto;
import com.owuor.somolink.network.entity.BridgeConfiguration;
import com.owuor.somolink.network.entity.PortConfiguration;
import com.owuor.somolink.users.dto.SchoolUserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class SchoolResponse {
    private Long id;
    private String name;
    private String code;
    private String location;
    private boolean active;
    private List<DeviceResponse> devices;
    private List<SchoolUserResponse> users;
    private BridgeConfigurationResponseDto bridgeConfiguration;
}
