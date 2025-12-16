package com.owuor.somolink.auth.repository;

import com.owuor.somolink.auth.entity.User;
import com.owuor.somolink.auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsBySchoolIdAndRole(Long schoolId, Role role);
}
