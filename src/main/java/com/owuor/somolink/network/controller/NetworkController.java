package com.owuor.somolink.network.controller;

import com.owuor.somolink.dev.RouterOSTestClient;
import com.owuor.somolink.network.config.RouterOSClient;
import com.owuor.somolink.network.dto.BridgeConfigurationResponseDto;
import com.owuor.somolink.network.dto.ConfigureBridgeRequest;
import com.owuor.somolink.network.dto.PortConfigurationResponseDto;
import com.owuor.somolink.network.service.BridgeConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/network")
public class NetworkController {

    private final BridgeConfigurationService bridgeConfigurationService;
    private final RouterOSClient routerOSClient;

    public NetworkController(BridgeConfigurationService bridgeConfigurationService, RouterOSClient routerOSClient) {
        this.bridgeConfigurationService = bridgeConfigurationService;
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

        bridgeConfigurationService.configureBridge(request, schoolId);
        return ResponseEntity.ok("Bridge configured successfully");
    }

    @GetMapping("/bridge/configuration/{schoolId}")
    public ResponseEntity<BridgeConfigurationResponseDto> getBridgeConfiguration( @PathVariable Long schoolId) throws Exception {
        return ResponseEntity.ok( bridgeConfigurationService.getBridgeConfiguration(schoolId));
    }

    @GetMapping("/interfaces")
    public ResponseEntity<List<String>> getInterfaces() throws Exception {
        return ResponseEntity.ok(bridgeConfigurationService.listInterfaces());
    }

    @GetMapping("/port/configurations")
    public ResponseEntity<List<BridgeConfigurationResponseDto>> getBridgeConfigurations() throws Exception {
        return ResponseEntity.ok(bridgeConfigurationService.bridgeConfigurations());
    }

    @GetMapping("/router/interfaces")
    public List<Map<String, String>> getAllInterfaces() {
        return RouterOSTestClient.listAllInterfaces();
    }
    @PostMapping("/interface/{name}/enable")
    public ResponseEntity<String> enableInterface(@PathVariable String name) {
        RouterOSTestClient.enableInterface(name);
        return ResponseEntity.ok("Enabled " + name);
    }

    @PostMapping("/interface/{name}/disable")
    public ResponseEntity<String> disableInterface(@PathVariable String name) {
        RouterOSTestClient.disableInterface(name);
        return ResponseEntity.ok("Disabled " + name);
    }


}
