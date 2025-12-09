package com.example.game_shop.user.dto.Response;

import lombok.*;
import java.time.*;

import com.example.game_shop.user.domain.User;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
