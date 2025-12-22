package com.owuor.somolink.network.repository;

import com.owuor.somolink.network.entity.WlanConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WlanConfigurationRepository extends JpaRepository<WlanConfiguration, Long> {
    List<WlanConfiguration> findBySchoolId(Long schoolId);
}
