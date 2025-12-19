package com.owuor.somolink.network.controller;

import com.owuor.somolink.network.dto.HotspotServerProfileRequest;
import com.owuor.somolink.network.dto.UserProfileRequest;
import com.owuor.somolink.network.dto.HotspotSetupRequest;
import com.owuor.somolink.network.entity.UserProfile;
import com.owuor.somolink.network.service.HotspotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotspot")
public class HotspotController {

    private final HotspotService hotspotService;

    public HotspotController(HotspotService hotspotService) {
        this.hotspotService = hotspotService;
    }

    /** Create a new Hotspot profile */
    @PostMapping("/create/user/profile")
    public ResponseEntity<?> createUserProfile(@Valid @RequestBody UserProfileRequest request) {
        try {
            return ResponseEntity.ok(hotspotService.createUserProfile(request));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Failed to create profile: " + ex.getMessage());
        }
    }

    /** Get all user hotspot profiles */
    @GetMapping("/user/profiles")
    public ResponseEntity<List<UserProfile>> getAllUserProfiles() {
        return ResponseEntity.ok(hotspotService.getAllUserProfiles());
    }

    /** Get user hotspot profile by ID */
    @GetMapping("/user/profile/{id}")
    public ResponseEntity<UserProfile> getUserProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(hotspotService.getUserProfileById(id));
    }

    /**
     * Create a MikroTik hotspot server profile from a port configuration
     */
    @PostMapping("/create/server/profile/{portConfigurationId}")
    public ResponseEntity<String> createHotspotProfile(@Valid @RequestBody HotspotServerProfileRequest request,@PathVariable Long portConfigurationId) {
        try {
            System.out.println("[DEBUG] Received request to create hotspot profile: " + request);

            hotspotService.createHotspotServerProfile(portConfigurationId, request.getProfileName(),request.getDnsName());

            return ResponseEntity.ok("Hotspot server profile created successfully: " + request.getProfileName());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating hotspot profile: " + e.getMessage());
        }
    }

    /** Setup hotspot on a given interface */
    @PostMapping("/setup/{portConfigurationId}")
    public ResponseEntity<String> setupHotspot(@Valid @RequestBody HotspotSetupRequest request, @PathVariable Long portConfigurationId , String parameter) {
        try {
            hotspotService.setupHotspotOnInterface(request,portConfigurationId);
            return ResponseEntity.ok("Hotspot setup successfully : " + request.getHotspotName());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Failed to setup hotspot: " + ex.getMessage());
        }
    }

    /** Setup hotspot on a given bridge */
    @PostMapping("/setup/{bridgeConfigurationId}")
    public ResponseEntity<String> setupHotspotOnBridge(@Valid @RequestBody HotspotSetupRequest request, @PathVariable Long bridgeConfigurationId) {
        try {
            hotspotService.setupHotspotOnBridgeInterface(request,bridgeConfigurationId);
            return ResponseEntity.ok("Hotspot setup successfully on interface: " + request.getHotspotName());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Failed to setup hotspot: " + ex.getMessage());
        }
    }


}
