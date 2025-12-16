package com.owuor.somolink.network.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HotspotLoginResponse {


    private String password;

    private String username;
}
