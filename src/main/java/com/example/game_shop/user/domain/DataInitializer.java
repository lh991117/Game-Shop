package com.example.game_shop.user.domain;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.game_shop.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository) {
        return args -> {
            if (!userRepository.existsByEmail("admin@test.com")) {
                User admin = User.builder()
                        .email("admin@test.com")
                        .password(passwordEncoder.encode("admin1234"))
                        .nickname("admin")
                        .role(UserRole.ROLE_ADMIN)
                        .build();

                userRepository.save(admin);
            }
        };
    }
}
