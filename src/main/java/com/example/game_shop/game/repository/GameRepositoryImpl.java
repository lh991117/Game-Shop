package com.example.game_shop.game.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.example.game_shop.game.domain.Game;
import com.example.game_shop.game.domain.GameGenre;
import com.example.game_shop.game.domain.GameSearchCondition;
import com.example.game_shop.game.domain.GameSortType;
import com.example.game_shop.game.domain.GameStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import static com.example.game_shop.game.domain.QGame.game;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GameRepositoryImpl implements GameRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Game> searchForUser(GameSearchCondition condition, GameSortType sortType, Pageable pageable) {
        BooleanBuilder where = baseWhere(condition);

        where.and(game.status.eq(GameStatus.ON_SALE));

        return executeSearch(where, sortType, pageable);
    }

    @Override
    public Page<Game> searchForAdmin(GameSearchCondition condition, GameSortType sortType, Pageable pageable) {
        BooleanBuilder where = baseWhere(condition);

        return executeSearch(where, sortType, pageable);
    }

    private Page<Game> executeSearch(BooleanBuilder where, GameSortType sortType, Pageable pageable) {
        OrderSpecifier<?> order = orderBy(sortType);

        List<Game> content = queryFactory
                .selectFrom(game)
                .where(where)
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(game.count())
                .from(game)
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanBuilder baseWhere(GameSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(keywordPredicate(condition.getKeyword()));
        builder.and(eqPlatform(condition.getPlatform()));
        builder.and(eqGenre(condition.getGenre()));
        builder.and(gtePrice(condition.getMinPrice()));
        builder.and(ltePrice(condition.getMaxPrice()));

        return builder;
    }

    private BooleanExpression keywordPredicate(String keyword) {
        if (keyword == null || keyword.isBlank())
            return null;

        String normalized = keyword.trim().replaceAll("\\s+", " ");
        String[] tokens = normalized.split(" ");

        BooleanExpression expr = null;
        for (String token : tokens) {
            BooleanExpression tokenExpr = game.title.containsIgnoreCase(token)
                    .or(game.description.containsIgnoreCase(token));
            expr = (expr == null) ? tokenExpr : expr.and(tokenExpr);
        }
        return expr;
    }

    private BooleanExpression eqPlatform(String platform) {
        if (platform == null || platform.isBlank())
            return null;

        return game.platform.eq(platform);
    }

    private BooleanExpression eqGenre(GameGenre genre) {
        if (genre == null)
            return null;

        return game.genre.eq(genre);
    }

    private BooleanExpression gtePrice(Integer minPrice) {
        if (minPrice == null)
            return null;

        return game.price.goe(minPrice);
    }

    private BooleanExpression ltePrice(Integer maxPrice) {
        if (maxPrice == null)
            return null;

        return game.price.loe(maxPrice);
    }

    private OrderSpecifier<?> orderBy(GameSortType sortType) {
        return switch (sortType) {
            case PRICE_ASC -> game.price.asc();
            case PRICE_DESC -> game.price.desc();
            case LATEST -> game.createdAt.desc();
        };
    }

}
