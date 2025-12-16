package com.owuor.somolink.network.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConfigureBridgeRequest {
    private List<String> interfaces; // interfaces to add to bridge
    private String ipAddress;
    private int subnetMask;
    private String description;
}
