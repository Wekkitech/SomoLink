package com.owuor.somolink.seed;



import com.owuor.somolink.auth.entity.User;
import com.owuor.somolink.auth.enums.Role;
import com.owuor.somolink.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin123!"))
                    .role(Role.ISP_ADMIN)
                    .username("admin")
                    .school(null)
                    .build();

            userRepository.save(admin);
            System.out.println("✔️ Admin user seeded successfully.");
        }
    }
}
