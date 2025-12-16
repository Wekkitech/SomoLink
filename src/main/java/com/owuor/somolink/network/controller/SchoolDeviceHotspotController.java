package com.owuor.somolink.network.controller;

import com.owuor.somolink.network.dto.HotspotLoginResponse;
import com.owuor.somolink.network.dto.SchoolDeviceLoginRequest;
import com.owuor.somolink.network.service.SchoolDeviceHotspotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotspot")
@RequiredArgsConstructor
public class SchoolDeviceHotspotController {

    private final SchoolDeviceHotspotService service;

    @PostMapping("/school-login")
    public ResponseEntity<HotspotLoginResponse> schoolLogin(
            @Valid @RequestBody SchoolDeviceLoginRequest request

    ) throws Exception {

        return ResponseEntity.ok(
                service.loginSchoolDevice(request)
        );
    }
}
