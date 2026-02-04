package com.group4.backend.config;

import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Create Admin User if not exists
            if (!userRepository.existsByEmail("admin@collabry.com")) {
                User admin = new User(
                        "admin@collabry.com",
                        passwordEncoder.encode("password123"),
                        Role.ADMIN);
                userRepository.save(admin);
                System.out.println("Admin user created: admin@collabry.com / password123");
            }
        };
    }
}
