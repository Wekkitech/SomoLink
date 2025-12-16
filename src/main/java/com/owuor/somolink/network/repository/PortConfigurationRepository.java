package com.owuor.somolink.network.repository;

import com.owuor.somolink.network.entity.PortConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortConfigurationRepository extends JpaRepository<PortConfiguration, Long> {
    boolean existsByPortName(String portName);
}
