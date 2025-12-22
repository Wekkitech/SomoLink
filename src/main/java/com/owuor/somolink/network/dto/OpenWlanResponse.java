package com.owuor.somolink.network.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OpenWlanResponse {
    private String ssidName;
    private String wlanInterface;
}
