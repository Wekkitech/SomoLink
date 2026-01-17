package com.owuor.somolink.analytics.service;

import com.owuor.somolink.network.config.RouterOSClient;
import com.owuor.somolink.network.dto.RouterHealth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RouterHealthService {
    private final RouterOSClient routerOSClient; // Inject your client


    public RouterHealth getMainRouterHealth() {
        return routerOSClient.getRouterHealth();
    }
}
