package com.owuor.somolink.network.repository;

import com.owuor.somolink.school.entity.Device;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolDeviceRepository extends JpaRepository<Device,Long> {
    Optional<Device> findByMacAddressAndSchoolCode(String mac, String schoolCode);
}
