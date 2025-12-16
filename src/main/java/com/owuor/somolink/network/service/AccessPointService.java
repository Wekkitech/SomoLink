package com.owuor.somolink.network.service;

import com.owuor.somolink.network.dto.CreateAccessPointRequest;
import com.owuor.somolink.network.entity.AccessPoint;
import com.owuor.somolink.network.entity.Hotspot;
import com.owuor.somolink.network.repository.AccessPointRepository;
import com.owuor.somolink.network.repository.HotspotRepository;
import org.springframework.stereotype.Service;

@Service
public class AccessPointService {

    private final AccessPointRepository apRepository;
    private final HotspotRepository hotspotRepository;

    public AccessPointService(AccessPointRepository apRepository, HotspotRepository hotspotRepository) {
        this.apRepository = apRepository;
        this.hotspotRepository = hotspotRepository;
    }

    public AccessPoint registerAP(CreateAccessPointRequest request, long hotspotId) {

        Hotspot hotspot = hotspotRepository.findById(hotspotId)
                .orElseThrow(() -> new IllegalArgumentException("Hotspot not found with id: " + hotspotId));

        AccessPoint ap = new AccessPoint();
        ap.setApName(request.getApName());
        ap.setHotspot(hotspot);
        ap.setBridgeMode(request.isBridgeMode());
        ap.setMacAddress(request.getMacAddress());
        ap.setModel(request.getModel());
        ap.setSchoolName(request.getSchoolName());
        ap.setIpAddress(request.getIpAddress());
        ap.setDescription(request.getDescription());

        return apRepository.save(ap);
    }
}
