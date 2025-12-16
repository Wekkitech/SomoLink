package com.owuor.somolink.network.dto;

import lombok.Data;

@Data
public class HotspotServerProfileRequest {
    private String profileName;  // Name for the hotspot server profile
    private String dnsName; //Dns name for the hotspot
}
