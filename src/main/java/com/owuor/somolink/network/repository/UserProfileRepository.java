package com.owuor.somolink.network.repository;

import com.owuor.somolink.network.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByProfileName(String profileName);
}
