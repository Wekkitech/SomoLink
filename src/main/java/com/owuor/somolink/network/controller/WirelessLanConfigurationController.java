package com.owuor.somolink.network.controller;

import com.owuor.somolink.network.dto.OpenWlanRequest;
import com.owuor.somolink.network.dto.OpenWlanResponse;
import com.owuor.somolink.network.service.WirelessLanConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/network/wlan")
@RequiredArgsConstructor
@Validated
public class WirelessLanConfigurationController {

    private final WirelessLanConfigurationService wlanService;

    @PostMapping("/setup/{schoolId}")
    public ResponseEntity<?> setupOpenWlan(@Valid @RequestBody OpenWlanRequest request, @PathVariable Long schoolId) {
        try {
            wlanService.SetUpWlan(schoolId, request.getSsidName(), request.getWlanInterface());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Open WLAN configured successfully");
            response.put("ssid", request.getSsidName());
            response.put("interface", request.getWlanInterface());

            return ResponseEntity.ok(response);

        } catch (Exception ex) {

            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", "Failed to configure WLAN");
            error.put("details", ex.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{schoolId}")
    public ResponseEntity<List<OpenWlanResponse>> getOpenWlans(@PathVariable Long schoolId) throws Exception {

        return ResponseEntity.ok(wlanService.getOpenWlan(schoolId));


    }


}
