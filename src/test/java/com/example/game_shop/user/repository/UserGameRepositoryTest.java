package com.example.game_shop.user.repository;

import com.example.game_shop.game.domain.Game;
import com.example.game_shop.game.domain.GameGenre;
import com.example.game_shop.game.domain.GameStatus;
import com.example.game_shop.game.repository.GameRepository;
import com.example.game_shop.global.config.QuerydslConfig;
import com.example.game_shop.user.domain.UserGame;
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
class UserGameRepositoryTest {

    @Autowired
    private UserGameRepository userGameRepository;

    @Autowired
    private GameRepository gameRepository;

    private Game savedGame1;
    private Game savedGame2;
    private final Long USER_ID = 1L;
    private final Long OTHER_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        userGameRepository.deleteAll();
        gameRepository.deleteAll();

        savedGame1 = gameRepository.save(buildGame("엘든 링", 64800, GameGenre.RPG));
        savedGame2 = gameRepository.save(buildGame("오버워치2", 0, GameGenre.FPS));

        userGameRepository.save(UserGame.of(USER_ID, savedGame1, savedGame1.getPrice()));
        userGameRepository.save(UserGame.of(USER_ID, savedGame2, savedGame2.getPrice()));
    }

    @Nested
    @DisplayName("게임 보유 여부 확인 (existsByUserIdAndGame_Id)")
    class ExistsByUserIdAndGameId {

        @Test
        @DisplayName("구매한 게임이면 true를 반환한다")
        void exists_true() {
            assertThat(userGameRepository.existsByUserIdAndGame_Id(USER_ID, savedGame1.getId())).isTrue();
        }

        @Test
        @DisplayName("구매하지 않은 게임이면 false를 반환한다")
        void exists_false() {
            Game newGame = gameRepository.save(buildGame("미구매 게임", 10000, GameGenre.ACTION));
            assertThat(userGameRepository.existsByUserIdAndGame_Id(USER_ID, newGame.getId())).isFalse();
        }

        @Test
        @DisplayName("다른 유저가 구매한 게임은 false를 반환한다")
        void exists_otherUser() {
            assertThat(userGameRepository.existsByUserIdAndGame_Id(OTHER_USER_ID, savedGame1.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("내 라이브러리 조회 (findMyLibrary)")
    class FindMyLibrary {

        @Test
        @DisplayName("내 구매 목록만 반환된다")
        void findMyLibrary_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<UserGame> result = userGameRepository.findMyLibrary(USER_ID, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(ug -> ug.getUserId().equals(USER_ID));
        }

        @Test
        @DisplayName("다른 유저의 구매 목록은 조회되지 않는다")
        void findMyLibrary_otherUser_empty() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<UserGame> result = userGameRepository.findMyLibrary(OTHER_USER_ID, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("페이징이 올바르게 동작한다")
        void findMyLibrary_paging() {
            // given
            Pageable pageable = PageRequest.of(0, 1);

            // when
            Page<UserGame> result = userGameRepository.findMyLibrary(USER_ID, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("game 정보가 fetch join으로 함께 로드된다")
        void findMyLibrary_gameFetched() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<UserGame> result = userGameRepository.findMyLibrary(USER_ID, pageable);

            // then - LazyInitializationException 없이 game 접근 가능
            assertThat(result.getContent().get(0).getGame()).isNotNull();
            assertThat(result.getContent().get(0).getGame().getTitle()).isNotBlank();
        }
    }

    // --- 헬퍼 메서드 ---

    private Game buildGame(String title, int price, GameGenre genre) {
        return Game.builder()
                .title(title)
                .price(price)
                .platform("PC")
                .genre(genre)
                .description("설명")
                .status(GameStatus.ON_SALE)
                .build();
    }
}
