package com.example.game_shop.game.repository;

import com.example.game_shop.game.domain.*;
import com.example.game_shop.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
class GameRepositoryImplTest {

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();

        gameRepository.save(buildGame("엘든 링", 64800, "PC", GameGenre.RPG, "오픈월드 RPG 게임", GameStatus.ON_SALE));
        gameRepository.save(buildGame("다크 소울", 39800, "PC", GameGenre.ACTION, "소울라이크 액션 게임", GameStatus.ON_SALE));
        gameRepository.save(buildGame("오버워치2", 0, "PC", GameGenre.FPS, "팀 기반 FPS 게임", GameStatus.ON_SALE));
        gameRepository.save(buildGame("FIFA 24", 79800, "PS5", GameGenre.SPORTS, "축구 스포츠 게임", GameStatus.ON_SALE));
        gameRepository.save(buildGame("숨겨진 게임", 10000, "PC", GameGenre.ACTION, "숨겨진 게임 설명", GameStatus.HIDDEN));
        gameRepository.save(buildGame("단종 게임", 20000, "PC", GameGenre.RPG, "판매 종료 게임", GameStatus.DELISTED));
    }

    @Nested
    @DisplayName("일반 사용자용 검색 (searchForUser)")
    class SearchForUser {

        @Test
        @DisplayName("ON_SALE 게임만 반환된다")
        void searchForUser_onlyOnSale() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getContent()).hasSize(4);
            assertThat(result.getContent()).allMatch(g -> g.getStatus() == GameStatus.ON_SALE);
        }

        @Test
        @DisplayName("키워드로 제목 검색이 가능하다")
        void searchForUser_keywordInTitle() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            condition.setKeyword("엘든");
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("엘든 링");
        }

        @Test
        @DisplayName("키워드로 설명(description) 검색이 가능하다")
        void searchForUser_keywordInDescription() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            condition.setKeyword("소울라이크");
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("다크 소울");
        }

        @Test
        @DisplayName("장르로 필터링이 가능하다")
        void searchForUser_genreFilter() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            condition.setGenre(GameGenre.RPG);
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).allMatch(g -> g.getGenre() == GameGenre.RPG);
        }

        @Test
        @DisplayName("플랫폼으로 필터링이 가능하다")
        void searchForUser_platformFilter() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            condition.setPlatform("PS5");
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("FIFA 24");
        }

        @Test
        @DisplayName("최소 가격 필터링이 가능하다")
        void searchForUser_minPriceFilter() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            condition.setMinPrice(50000);
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getContent()).allMatch(g -> g.getPrice() >= 50000);
        }

        @Test
        @DisplayName("최대 가격 필터링이 가능하다")
        void searchForUser_maxPriceFilter() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            condition.setMaxPrice(10000);
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getContent()).allMatch(g -> g.getPrice() <= 10000);
        }

        @Test
        @DisplayName("가격 오름차순 정렬이 가능하다")
        void searchForUser_sortByPriceAsc() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.PRICE_ASC, pageable);

            // then
            int prev = -1;
            for (Game g : result.getContent()) {
                assertThat(g.getPrice()).isGreaterThanOrEqualTo(prev);
                prev = g.getPrice();
            }
        }

        @Test
        @DisplayName("가격 내림차순 정렬이 가능하다")
        void searchForUser_sortByPriceDesc() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.PRICE_DESC, pageable);

            // then
            int prev = Integer.MAX_VALUE;
            for (Game g : result.getContent()) {
                assertThat(g.getPrice()).isLessThanOrEqualTo(prev);
                prev = g.getPrice();
            }
        }

        @Test
        @DisplayName("조건이 없으면 모든 ON_SALE 게임이 반환된다")
        void searchForUser_noCondition() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("페이징이 올바르게 동작한다")
        void searchForUser_paging() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            Pageable pageable = PageRequest.of(0, 2);

            // when
            Page<Game> result = gameRepository.searchForUser(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("관리자용 검색 (searchForAdmin)")
    class SearchForAdmin {

        @Test
        @DisplayName("HIDDEN, DELISTED 포함 모든 게임이 반환된다")
        void searchForAdmin_allStatus() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForAdmin(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(6);
        }

        @Test
        @DisplayName("키워드 검색 시 모든 상태의 게임에서 검색된다")
        void searchForAdmin_keywordSearch() {
            // given
            GameSearchCondition condition = new GameSearchCondition();
            condition.setKeyword("숨겨진");
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<Game> result = gameRepository.searchForAdmin(condition, GameSortType.LATEST, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(GameStatus.HIDDEN);
        }
    }

    // --- 헬퍼 메서드 ---

    private Game buildGame(String title, int price, String platform,
                           GameGenre genre, String description, GameStatus status) {
        return Game.builder()
                .title(title)
                .price(price)
                .platform(platform)
                .genre(genre)
                .description(description)
                .status(status)
                .build();
    }
}
