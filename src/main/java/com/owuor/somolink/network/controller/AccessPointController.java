package com.owuor.somolink.network.controller;

import com.owuor.somolink.network.dto.CreateAccessPointRequest;
import com.owuor.somolink.network.entity.AccessPoint;
import com.owuor.somolink.network.service.AccessPointService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aps")
public class AccessPointController {

    private final AccessPointService apService;

    public AccessPointController(AccessPointService apService) {
        this.apService = apService;
    }

    @PostMapping("/register/{hotspotId}")
    public ResponseEntity<AccessPoint> registerAP(@Valid @RequestBody CreateAccessPointRequest request ,@PathVariable long hotspotId) {
        AccessPoint ap = apService.registerAP(request,hotspotId);
        return ResponseEntity.ok(ap);
    }
}
