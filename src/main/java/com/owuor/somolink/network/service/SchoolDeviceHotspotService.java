package com.owuor.somolink.network.service;

import com.owuor.somolink.network.config.RouterOSClient;
import com.owuor.somolink.network.dto.HotspotLoginResponse;
import com.owuor.somolink.network.dto.SchoolDeviceLoginRequest;
import com.owuor.somolink.network.repository.SchoolDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolDeviceHotspotService {

    private final SchoolDeviceRepository deviceRepository;
    private final RouterOSClient routerOSClient;

    private static final String SCHOOL_PROFILE = "Student Access - 2 Mbps - School Day";

    public HotspotLoginResponse loginSchoolDevice(
            SchoolDeviceLoginRequest request

    ) throws Exception {

        deviceRepository
                .findByMacAddressAndSchoolCode(
                        request.getMac(),
                        request.getSchoolCode()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Device not registered for this school")
                );

        // 🔑 Generate hotspot credentials
        String username = "school-" + request.getMac().replace(":", "");
        String password = UUID.randomUUID().toString().substring(0, 8);

        // 🔥 Create hotspot user
        routerOSClient.createOrUpdateHotspotUser(
                username,
                password,
                SCHOOL_PROFILE
        );

        return new HotspotLoginResponse(username, password);
    }
}
