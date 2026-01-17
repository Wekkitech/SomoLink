package com.owuor.somolink.network.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfig {

    @Bean
    public RouterOSClient routerOSClient() {
        return new RouterOSClient("41.84.146.142", "chris", "Brukhie@20");
    }
}
