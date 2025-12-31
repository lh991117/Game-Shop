package com.example.game_shop.game.dto.request;

import com.example.game_shop.game.domain.GameStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GameStatusUpdateRequest {
    
    @NotNull(message = "status는 필수입니다.")
    private GameStatus status;
}
