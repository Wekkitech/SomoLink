package com.owuor.somolink.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrafficResponse {
    private String interfaceName;
    private long rxBps;
    private long txBps;
    private long totalBps; // sum of rx + tx
    private long timestamp; // epoch millis
}
