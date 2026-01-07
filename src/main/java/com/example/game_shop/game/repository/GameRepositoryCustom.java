package com.example.game_shop.game.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.game_shop.game.domain.Game;
import com.example.game_shop.game.domain.GameSearchCondition;
import com.example.game_shop.game.domain.GameSortType;

public interface GameRepositoryCustom {
    Page<Game> searchForUser(GameSearchCondition condition, GameSortType sortType, Pageable pageable);

    Page<Game> searchForAdmin(GameSearchCondition condition, GameSortType sortType, Pageable pageable);
}
