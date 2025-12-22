package com.example.game_shop.game.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameCreateRequest {
    
    private String title;
    private int price;
    private String platform;
    private String genre;
    private String description;
}
