package com.example.game_shop.game.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.game_shop.game.domain.Game;
import com.example.game_shop.game.domain.GameSearchCondition;
import com.example.game_shop.game.domain.GameSortType;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GameRepositoryImpl implements GameRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Game> searchForUser(GameSearchCondition condition, GameSortType sortType, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'searchForUser'");
    }

    @Override
    public Page<Game> searchForAdmin(GameSearchCondition condition, GameSortType sortType, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'searchForAdmin'");
    }

}
