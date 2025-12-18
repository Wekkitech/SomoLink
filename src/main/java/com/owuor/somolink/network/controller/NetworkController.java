package com.owuor.somolink.network.controller;

import com.owuor.somolink.network.config.RouterOSClient;
import com.owuor.somolink.network.dto.ConfigureBridgeRequest;
import com.owuor.somolink.network.dto.ConfigurePortRequest;
import com.owuor.somolink.network.service.PortConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/network")
public class NetworkController {

    private final PortConfigurationService portService;
    private final RouterOSClient routerOSClient;

    public NetworkController(PortConfigurationService portService,  RouterOSClient routerOSClient) {
        this.portService = portService;
        this.routerOSClient = routerOSClient;
    }

    @GetMapping("/test-connection")
    public ResponseEntity<String> testConnection() {
        try {
            boolean isConnected = routerOSClient.testConnection();
            if (isConnected) {
                return ResponseEntity.ok("Router connection successful!");
            } else {
                return ResponseEntity.status(500).body("Router connection failed!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Router connection error: " + e.getMessage());
        }
    }


    @PostMapping("/configure/bridge/{schoolId}")
    public ResponseEntity<?> configureBridge(@RequestBody ConfigureBridgeRequest request, @PathVariable Long schoolId) throws Exception {

        portService.configureBridge(request, schoolId);
        return ResponseEntity.ok("Bridge configured successfully");
    }


    @GetMapping("/interfaces")
    public ResponseEntity<List<String>> getInterfaces() throws Exception {
        return ResponseEntity.ok(portService.listInterfaces());
    }



}
