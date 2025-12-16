package com.owuor.somolink.network.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HotspotSetupRequest {
    @NotBlank
    private String hotspotName;            // optional, default same as interface

}
