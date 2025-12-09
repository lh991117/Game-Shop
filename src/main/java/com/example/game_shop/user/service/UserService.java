package com.example.game_shop.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game_shop.global.exception.BadRequestException;
import com.example.game_shop.user.domain.User;
import com.example.game_shop.user.dto.Request.UserSignUpRequest;
import com.example.game_shop.user.dto.Response.UserResponse;
import com.example.game_shop.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse signUp(UserSignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(request.getPassword())
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }
}
