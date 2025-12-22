package com.owuor.somolink.network.service;

import com.owuor.somolink.network.config.RouterOSClient;
import com.owuor.somolink.network.entity.WlanConfiguration;
import com.owuor.somolink.network.repository.WlanConfigurationRepository;
import com.owuor.somolink.school.entity.School;
import com.owuor.somolink.school.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WirelessLanConfigurationService {


    private final RouterOSClient routerClient;
    private final WlanConfigurationRepository wlanRepository;
    private final SchoolRepository schoolRepository;

    public void SetUpWlan(Long schoolId, String ssidName, String wlanInterface) throws Exception {

        routerClient.setupOpenWlan(wlanInterface,ssidName);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new Exception("School not found"));

        // Create WLAN entity
        WlanConfiguration wlan = new WlanConfiguration();
        wlan.setSchool(school);
        wlan.setWlanInterface(wlanInterface);
        wlan.setSsidName(ssidName);
        wlan.setConfigured(true); // mark as configured

        wlanRepository.save(wlan);

    }
}
