package com.owuor.somolink.analytics.service;

import com.owuor.somolink.analytics.dto.TrafficResponse;
import com.owuor.somolink.network.config.RouterOSClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrafficService {

    private final RouterOSClient routerOSClient; // Inject your client

    public TrafficResponse getInterfaceTraffic(String iface) {
        try {
            Map<String, Long> traffic = routerOSClient.getInterfaceTraffic(iface);
            long rx = traffic.getOrDefault("rxBps", 0L);
            long tx = traffic.getOrDefault("txBps", 0L);
            return new TrafficResponse(
                    iface,
                    rx,
                    tx,
                    rx + tx,
                    System.currentTimeMillis()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get traffic for " + iface, e);
        }
    }
}
