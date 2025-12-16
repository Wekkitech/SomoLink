package com.owuor.somolink.users.service;

import com.owuor.somolink.auth.entity.User;
import com.owuor.somolink.auth.enums.Role;
import com.owuor.somolink.auth.repository.UserRepository;
import com.owuor.somolink.school.entity.School;
import com.owuor.somolink.school.repository.SchoolRepository;
import com.owuor.somolink.users.dto.RegisterSchoolUserRequest;
import com.owuor.somolink.users.dto.SchoolUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolUserService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SchoolUserResponse addUserToSchool(Long schoolId, RegisterSchoolUserRequest request) {

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        if (request.getRole() == Role.SCHOOL_ADMIN) {
            if (userRepository.existsBySchoolIdAndRole(schoolId, Role.SCHOOL_ADMIN)) {
                throw new IllegalStateException("This school already has a principal");
            }
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .school(school)
                .build();

        User saved = userRepository.save(user);

        return new SchoolUserResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getRole(),
                saved.getSchool().getId()
        );
    }
}
