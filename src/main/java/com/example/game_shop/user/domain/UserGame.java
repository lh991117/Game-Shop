package com.example.game_shop.user.domain;

import java.time.LocalDateTime;

import com.example.game_shop.game.domain.Game;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_game", uniqueConstraints = @UniqueConstraint(name = "uk_user_game", columnNames = { "user_id",
        "game_id" }))
@Entity
public class UserGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    @Column(name = "price_paid", nullable = false)
    private int pricePaid;

    @Builder
    private UserGame(Long userId, Game game, LocalDateTime purchasedAt, int pricePaid) {
        this.userId = userId;
        this.game = game;
        this.purchasedAt = purchasedAt;
        this.pricePaid = pricePaid;
    }

    public static UserGame of(Long userId, Game game, int pricePaid) {
        return UserGame.builder()
                .userId(userId)
                .game(game)
                .pricePaid(pricePaid)
                .purchasedAt(LocalDateTime.now())
                .build();
    }
}
