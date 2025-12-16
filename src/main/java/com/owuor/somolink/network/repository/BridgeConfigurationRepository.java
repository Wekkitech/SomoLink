package com.owuor.somolink.network.repository;


import com.owuor.somolink.network.entity.BridgeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BridgeConfigurationRepository extends JpaRepository<BridgeConfiguration, Long> {
    boolean existsByBridgeName(String bridgeName);

    boolean existsBySchoolId(Long schoolId);
}
