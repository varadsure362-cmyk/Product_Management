package com.varad.productmanagement.config;

import com.varad.productmanagement.entity.Role;
import com.varad.productmanagement.entity.User;
import com.varad.productmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("varad")) {
            User admin = User.builder()
                    .username("varad")
                    .email("varad@admin.com")
                    .password(passwordEncoder.encode("varad@123"))
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user 'varad' created.");
        }
    }
}
