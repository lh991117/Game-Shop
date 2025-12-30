package com.example.game_shop.game.repository;

import java.util.Optional;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.game_shop.game.domain.Game;
import com.example.game_shop.game.domain.GameStatus;

public interface GameRepository extends JpaRepository<Game, Long> {

    Page<Game> findAllByStatus(GameStatus status, Pageable pageable);

    Optional<Game> findByIdAndStatus(Long id, GameStatus status);
}
