package com.owuor.somolink.network.dto;

import java.time.LocalDateTime;

public record HotspotResponseDto(
        Long id,
        String hotspotName,
        String interfaceName,
        Long profileId,
        String profileName,
        Long bridgeConfigurationId,
        String bridgePortName,
        boolean configured,
        LocalDateTime createdAt,
        LocalDateTime configuredAt
) {}
