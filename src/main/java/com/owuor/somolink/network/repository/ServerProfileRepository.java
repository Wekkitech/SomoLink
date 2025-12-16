package com.owuor.somolink.network.repository;

import com.owuor.somolink.network.entity.ServerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerProfileRepository extends JpaRepository<ServerProfile, Long> {
    Optional<ServerProfile> findByProfileName(String profileName);

    Optional<ServerProfile>  findByHotspotAddress(String gatewayIp);
}
