package com.owuor.somolink.analytics.controller;

import com.owuor.somolink.analytics.dto.TrafficResponse;
import com.owuor.somolink.analytics.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficService trafficService;

    @GetMapping("/{iface}")
    public TrafficResponse getTraffic(@PathVariable String iface) {
        System.out.println(iface);
        return trafficService.getInterfaceTraffic(iface);
    }
}
