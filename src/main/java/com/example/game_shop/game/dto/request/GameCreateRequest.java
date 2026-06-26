package com.example.game_shop.game.dto.request;

import com.example.game_shop.game.domain.GameGenre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameCreateRequest {

    @NotBlank(message = "게임 제목은 필수입니다.")
    private String title;

    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @NotBlank(message = "플랫폼은 필수입니다.")
    private String platform;

    @NotNull(message = "장르는 필수입니다.")
    private GameGenre genre;

    @NotBlank(message = "게임 설명은 필수입니다.")
    private String description;
}
