package com.example.game_shop.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game_shop.user.domain.User;
import com.example.game_shop.user.domain.UserRole;
import com.example.game_shop.user.dto.Request.UserSignUpRequest;
import com.example.game_shop.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(UserSignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .nickname(request.getNickname())
                .role(UserRole.ROLE_USER)
                .build();

        userRepository.save(user);
    }
}
