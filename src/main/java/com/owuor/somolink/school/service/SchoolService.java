package com.owuor.somolink.school.service;


import com.owuor.somolink.network.dto.BridgeConfigurationResponseDto;
import com.owuor.somolink.network.dto.OpenWlanResponse;
import com.owuor.somolink.network.dto.PortConfigurationResponseDto;
import com.owuor.somolink.network.entity.BridgeConfiguration;
import com.owuor.somolink.school.dto.CreateSchoolRequest;
import com.owuor.somolink.school.dto.DeviceResponse;
import com.owuor.somolink.school.dto.SchoolResponse;
import com.owuor.somolink.school.entity.Device;
import com.owuor.somolink.school.entity.School;
import com.owuor.somolink.school.repository.SchoolRepository;
import com.owuor.somolink.users.dto.SchoolUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;


    public SchoolResponse createSchool(CreateSchoolRequest req) {
        School school = new School();
        school.setName(req.getName());
        school.setLocation(req.getLocation());

        // Generate a unique school code
        String uniqueCode = "SCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        school.setCode(uniqueCode);

        School savedSchool = schoolRepository.save(school);

        return mapToDto(savedSchool);
    }


    public List<SchoolResponse> getAllSchools() {
        return schoolRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public SchoolResponse getSingleSchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("School not found with id: " + schoolId));


        return mapToDto(school);

    }

    public SchoolResponse mapToDto(School school) {
        SchoolResponse dto = new SchoolResponse();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setCode(school.getCode());
        dto.setLocation(school.getLocation());
        dto.setActive(school.isActive());
        dto.setBridgeId(school.getBridgeConfiguration().getId());

        return dto;
    }


    public List<DeviceResponse> getSchoolDevices(Long schoolId) {

        School school= schoolRepository.findById(schoolId).orElseThrow(
                () -> new IllegalArgumentException("School not found with id: " + schoolId)
        );
                List<Device> devices = school.getDevices();
                return  devices.stream()
                        .map(device-> {
                            DeviceResponse deviceResponse = new DeviceResponse();
                            deviceResponse.setId(device.getId());
                            deviceResponse.setMacAddress(device.getMacAddress());
                            deviceResponse.setDeviceName(device.getDeviceName());
                            deviceResponse.setSchoolName(school.getName());
                            return deviceResponse;

                        }).toList();



    }
}
