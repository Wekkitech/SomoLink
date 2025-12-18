package com.owuor.somolink.network.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfig {

    @Bean
    public RouterOSClient routerOSClient() {
        return new RouterOSClient("192.168.88.1", "admin", "admin");
    }
}
