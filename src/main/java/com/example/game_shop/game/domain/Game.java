package com.example.game_shop.game.domain;

import com.example.game_shop.global.common.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "games")
public class Game extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private int price;

    private String platform;

    private String genre;

    @Column(length = 1000)
    private String description;

    @Builder
    public Game(String title, int price, String platform, String genre, String description) {
        this.title = title;
        this.price = price;
        this.platform = platform;
        this.genre = genre;
        this.description = description;
    }
}
