package com.owuor.somolink.school.repository;

import com.owuor.somolink.school.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
    boolean existsByCode(String code);
}
