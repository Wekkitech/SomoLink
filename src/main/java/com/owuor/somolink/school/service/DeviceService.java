package com.owuor.somolink.school.service;


import com.owuor.somolink.school.dto.CreateDeviceRequest;
import com.owuor.somolink.school.dto.DeviceResponse;
import com.owuor.somolink.school.entity.Device;
import com.owuor.somolink.school.entity.School;
import com.owuor.somolink.school.repository.DeviceRepository;
import com.owuor.somolink.school.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final SchoolRepository schoolRepository;

    public DeviceResponse addDevice(Long schoolId, CreateDeviceRequest request) {

        if (deviceRepository.existsByMacAddress(request.getMacAddress())) {
            throw new IllegalArgumentException("Device with MAC already exists");
        }

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("School not found"));

        Device device = new Device();
        device.setDeviceName(request.getDeviceName());
        device.setMacAddress(request.getMacAddress());
        device.setSchool(school);

        Device saved = deviceRepository.save(device);

        return toResponse(saved);
    }

    private DeviceResponse toResponse(Device device) {
        DeviceResponse res = new DeviceResponse();
        res.setId(device.getId());
        res.setDeviceName(device.getDeviceName());
        res.setMacAddress(device.getMacAddress());
        return res;
    }
}
