package com.owuor.somolink.network.repository;


import com.owuor.somolink.network.entity.BridgeConfiguration;
import com.owuor.somolink.school.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BridgeConfigurationRepository extends JpaRepository<BridgeConfiguration, Long> {
    boolean existsByBridgeName(String bridgeName);

    boolean existsBySchoolId(Long schoolId);

    Optional<BridgeConfiguration>  findBySchoolId(Long schoolId);
}
