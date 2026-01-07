package com.example.game_shop.game.dto.request;

import com.example.game_shop.game.domain.GameGenre;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameCreateRequest {

    private String title;
    private int price;
    private String platform;
    private GameGenre genre;
    private String description;
}
