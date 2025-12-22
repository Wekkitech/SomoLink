package com.owuor.somolink.network.dto;


import java.time.LocalDateTime;

public record ServerProfileResponseDto(
        Long id,
        String profileName,
        String dnsName,
        String hotspotAddress,
        boolean configured,
        LocalDateTime createdAt,
        Long bridgeConfigurationId,
        String bridgePortName
) {
}
