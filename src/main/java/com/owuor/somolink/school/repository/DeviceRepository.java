package com.owuor.somolink.school.repository;


import com.owuor.somolink.school.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    boolean existsByMacAddress(String macAddress);
}
