package com.owuor.somolink.network.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateVlanRequest {
    private String name;          // VLAN name, e.g., "school103"
    private String gatewayIp;     // e.g., "10.103.0.1"
    private String subnetCidr;    // e.g., "10.103.0.0/24"
    private String parentInterface; // e.g., "ether1"
}
