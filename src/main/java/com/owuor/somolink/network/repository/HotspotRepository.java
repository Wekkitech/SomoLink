package com.owuor.somolink.network.repository;

import com.owuor.somolink.network.entity.Hotspot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotspotRepository extends JpaRepository<Hotspot, Long> {
    Optional<Hotspot> findByHotspotName(String hotspotName);
    // If only ONE hotspot per bridge
    Optional<Hotspot> findByBridgeConfiguration_Id(Long bridgeId);


}
