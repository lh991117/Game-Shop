package com.example.game_shop.user.dto.Response;

import java.time.LocalDateTime;

import com.example.game_shop.game.domain.Game;
import com.example.game_shop.user.domain.UserGame;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LibraryItemResponse {

    private Long gameId;
    private String title;
    private String platform;
    private String genre;
    private LocalDateTime purchasedAt;

    public static LibraryItemResponse from(UserGame ug) {
        Game g = ug.getGame();
        return LibraryItemResponse.builder()
                .gameId(g.getId())
                .title(g.getTitle())
                .platform(g.getPlatform())
                .genre(g.getGenre())
                .purchasedAt(ug.getPurchasedAt())
                .build();
    }
}
