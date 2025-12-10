package com.example.game_shop.game.dto;

import com.example.game_shop.game.domain.Game;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameResponse {

    private Long id;
    private String title;
    private int price;
    private String platform;
    private String genre;
    private String description;

    public static GameResponse from(Game game) {
        return GameResponse.builder()
                .id(game.getId())
                .title(game.getTitle())
                .price(game.getPrice())
                .platform(game.getPlatform())
                .genre(game.getGenre())
                .description(game.getDescription())
                .build();
    }
}
