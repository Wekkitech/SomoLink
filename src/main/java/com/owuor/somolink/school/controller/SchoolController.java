package com.owuor.somolink.school.controller;

import com.owuor.somolink.school.dto.CreateDeviceRequest;
import com.owuor.somolink.school.dto.CreateSchoolRequest;
import com.owuor.somolink.school.dto.DeviceResponse;
import com.owuor.somolink.school.dto.SchoolResponse;
import com.owuor.somolink.school.entity.School;
import com.owuor.somolink.school.service.DeviceService;
import com.owuor.somolink.school.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;
    private final DeviceService deviceService;

    @PostMapping("/create")
    public ResponseEntity<SchoolResponse> create(@Valid @RequestBody CreateSchoolRequest req) {
        return ResponseEntity.ok(schoolService.createSchool(req));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SchoolResponse>> all() {
        return ResponseEntity.ok().body(schoolService.getAllSchools());
    }

    @GetMapping("/{schoolId}")
    public ResponseEntity<SchoolResponse> SingleSchool(@PathVariable Long schoolId) {
        return ResponseEntity.ok().body(schoolService.getSingleSchool(schoolId));
    }


    @PostMapping("/add/device/{schoolId}")
    public ResponseEntity<?> addDevice(@Valid @RequestBody CreateDeviceRequest request, @PathVariable Long schoolId
    ) {
        return ResponseEntity.ok(deviceService.addDevice(schoolId, request));
    }

    @GetMapping("/devices/{schoolId}")
    public ResponseEntity<List<DeviceResponse>> getSchoolDevices(@PathVariable Long schoolId) {
        return ResponseEntity.ok().body(schoolService.getSchoolDevices(schoolId));
    }



}
