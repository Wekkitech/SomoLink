package com.owuor.somolink.network.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class OpenWlanResponse {
    private String ssidName;
    private String wlanInterface;


}
