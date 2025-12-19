package com.owuor.somolink.school.service;


import com.owuor.somolink.network.dto.BridgeConfigurationResponseDto;
import com.owuor.somolink.network.dto.PortConfigurationResponseDto;
import com.owuor.somolink.network.entity.BridgeConfiguration;
import com.owuor.somolink.school.dto.CreateSchoolRequest;
import com.owuor.somolink.school.dto.DeviceResponse;
import com.owuor.somolink.school.dto.SchoolResponse;
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

        // Bridge Configuration
        if (school.getBridgeConfiguration() != null) {
            BridgeConfigurationResponseDto bridgeConfigurationResponseDto = new BridgeConfigurationResponseDto();
            bridgeConfigurationResponseDto.setId(school.getBridgeConfiguration().getId());
            bridgeConfigurationResponseDto.setConfigured(school.getBridgeConfiguration().isConfigured());
            bridgeConfigurationResponseDto.setCidr(school.getBridgeConfiguration().getCidr());
            bridgeConfigurationResponseDto.setSubnetMask(school.getBridgeConfiguration().getSubnetMask());
            bridgeConfigurationResponseDto.setPortName(school.getBridgeConfiguration().getBridgeName());
            bridgeConfigurationResponseDto.setNetworkCidr(school.getBridgeConfiguration().getNetworkCidr());
            bridgeConfigurationResponseDto.setDhcpPoolRange(school.getBridgeConfiguration().getDhcpPoolRange());
            bridgeConfigurationResponseDto.setDhcpPoolName(school.getBridgeConfiguration().getDhcpPoolName());
            bridgeConfigurationResponseDto.setDescription(school.getBridgeConfiguration().getDescription());
            bridgeConfigurationResponseDto.setInterfaces(school.getBridgeConfiguration().getInterfaces());
            dto.setBridgeConfiguration(bridgeConfigurationResponseDto);

        }

        // Devices
        if (school.getDevices() != null) {
            List<DeviceResponse> deviceDtos = school.getDevices().stream().map(device -> {
                DeviceResponse d = new DeviceResponse();
                d.setId(device.getId());
                d.setMacAddress(device.getMacAddress());
                d.setSchoolName(school.getName());
                d.setDeviceName(device.getDeviceName());

                return d;
            }).toList();
            dto.setDevices(deviceDtos);
        }

        // Users
        if (school.getUsers() != null) {
            List<SchoolUserResponse> userDtos = school.getUsers().stream().map(user -> {
                SchoolUserResponse schoolUser = new SchoolUserResponse();
                schoolUser.setId(user.getId());
                schoolUser.setUsername(user.getUsername());
                schoolUser.setEmail(user.getEmail());
                schoolUser.setFirstName(user.getFirstName());
                schoolUser.setLastName(user.getLastName());
                schoolUser.setSchoolId(school.getId());
                schoolUser.setRole(user.getRole());
                return schoolUser;
            }).toList();
            dto.setUsers(userDtos);
        }

        return dto;
    }


}
