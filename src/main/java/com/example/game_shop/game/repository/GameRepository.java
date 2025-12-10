package com.example.game_shop.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.game_shop.game.domain.Game;

public interface GameRepository extends JpaRepository<Game, Long> {

}
