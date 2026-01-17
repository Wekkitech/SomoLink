package com.owuor.somolink.analytics.controller;

import com.owuor.somolink.analytics.service.RouterHealthService;
import com.owuor.somolink.network.dto.RouterHealth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/router")
public class RouterHealthController {


    private final RouterHealthService routerHealthService;

    @GetMapping("/health")
    public RouterHealth getRouterHealth() {
        return routerHealthService.getMainRouterHealth();
    }
}
