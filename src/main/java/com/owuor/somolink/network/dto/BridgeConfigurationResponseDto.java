package com.owuor.somolink.network.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class BridgeConfigurationResponseDto {
    private Long id;
    private String portName;
    private String cidr;
    private int subnetMask;
    private String networkCidr;
    private String dhcpPoolRange;
    private String dhcpPoolName;
    private String description;
    private boolean configured;
    private List<String> interfaces;
}
