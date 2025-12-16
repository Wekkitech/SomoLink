package com.owuor.somolink.network.repository;

import com.owuor.somolink.network.entity.Hotspot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotspotRepository extends JpaRepository<Hotspot, Long> {
    Optional<Hotspot> findByHotspotName(String hotspotName);
}
