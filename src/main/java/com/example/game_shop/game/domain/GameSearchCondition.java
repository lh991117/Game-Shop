package com.example.game_shop.game.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameSearchCondition {

    private String keyword;
    private String platform;
    private GameGenre genre;
    private Integer minPrice;
    private Integer maxPrice;
}
