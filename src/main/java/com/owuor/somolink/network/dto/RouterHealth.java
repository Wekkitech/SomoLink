package com.owuor.somolink.network.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RouterHealth {
    private boolean online;
    private int cpuUsage;
    private int ramUsage;
}
