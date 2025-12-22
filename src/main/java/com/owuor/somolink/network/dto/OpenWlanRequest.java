package com.owuor.somolink.network.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenWlanRequest {

    @NotBlank(message = "SSID name must not be blank")
    private String ssidName;

    @NotBlank(message = "Wireless interface must not be blank")
    private String wlanInterface;
}
