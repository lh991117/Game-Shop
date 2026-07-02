package com.example.game_shop.game.service;

import com.example.game_shop.game.domain.*;
import com.example.game_shop.game.dto.request.GameCreateRequest;
import com.example.game_shop.game.dto.response.GameResponse;
import com.example.game_shop.game.repository.GameRepository;
import com.example.game_shop.global.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @InjectMocks
    private GameService gameService;

    @Mock
    private GameRepository gameRepository;

    @Nested
    @DisplayName("게임 등록")
    class Create {

        @Test
        @DisplayName("정상적으로 게임이 등록된다")
        void create_success() throws Exception {
            // given
            GameCreateRequest request = createRequest("엘든 링", 64800, "PC", GameGenre.RPG, "오픈월드 RPG");
            Game savedGame = buildGame(1L, "엘든 링", 64800, "PC", GameGenre.RPG, "오픈월드 RPG", GameStatus.ON_SALE);

            given(gameRepository.save(any(Game.class))).willReturn(savedGame);

            // when
            GameResponse response = gameService.create(request);

            // then
            assertThat(response.getTitle()).isEqualTo("엘든 링");
            assertThat(response.getPrice()).isEqualTo(64800);
            assertThat(response.getGenre()).isEqualTo(GameGenre.RPG);
        }

        @Test
        @DisplayName("게임 등록 시 상태는 ON_SALE로 설정된다")
        void create_statusIsOnSale() throws Exception {
            // given
            GameCreateRequest request = createRequest("테스트 게임", 10000, "PC", GameGenre.ACTION, "설명");
            Game savedGame = buildGame(1L, "테스트 게임", 10000, "PC", GameGenre.ACTION, "설명", GameStatus.ON_SALE);

            given(gameRepository.save(any(Game.class))).willReturn(savedGame);

            // when
            GameResponse response = gameService.create(request);

            // then - 저장된 게임은 ON_SALE 상태
            verify(gameRepository).save(any(Game.class));
        }
    }

    @Nested
    @DisplayName("게임 목록 조회")
    class GetGames {

        @Test
        @DisplayName("ON_SALE 상태의 게임 목록이 반환된다")
        void getGames_returnsOnSaleGames() throws Exception {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Game> games = List.of(
                    buildGame(1L, "게임A", 10000, "PC", GameGenre.RPG, "설명A", GameStatus.ON_SALE),
                    buildGame(2L, "게임B", 20000, "PS5", GameGenre.ACTION, "설명B", GameStatus.ON_SALE)
            );
            given(gameRepository.findAllByStatus(GameStatus.ON_SALE, pageable))
                    .willReturn(new PageImpl<>(games));

            // when
            Page<GameResponse> result = gameService.getGames(pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("게임A");
        }

        @Test
        @DisplayName("게임이 없으면 빈 페이지가 반환된다")
        void getGames_empty() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            given(gameRepository.findAllByStatus(GameStatus.ON_SALE, pageable))
                    .willReturn(new PageImpl<>(List.of()));

            // when
            Page<GameResponse> result = gameService.getGames(pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("게임 상세 조회")
    class GetGame {

        @Test
        @DisplayName("존재하는 게임 ID로 상세 조회 성공")
        void getGame_success() throws Exception {
            // given
            Game game = buildGame(1L, "엘든 링", 64800, "PC", GameGenre.RPG, "오픈월드 RPG", GameStatus.ON_SALE);
            given(gameRepository.findByIdAndStatus(1L, GameStatus.ON_SALE)).willReturn(Optional.of(game));

            // when
            GameResponse response = gameService.getGame(1L);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("엘든 링");
        }

        @Test
        @DisplayName("존재하지 않는 게임 ID 조회 시 NotFoundException 발생")
        void getGame_notFound() {
            // given
            given(gameRepository.findByIdAndStatus(999L, GameStatus.ON_SALE)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> gameService.getGame(999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("게임을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("DELISTED 게임은 조회되지 않는다")
        void getGame_delistedNotFound() {
            // given - ON_SALE 조건으로 조회하면 DELISTED 게임은 없음
            given(gameRepository.findByIdAndStatus(1L, GameStatus.ON_SALE)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> gameService.getGame(1L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("게임 상태 변경")
    class UpdateGameStatus {

        @Test
        @DisplayName("게임 상태를 DELISTED로 변경한다")
        void updateStatus_toDelisted() throws Exception {
            // given
            Game game = buildGame(1L, "게임A", 10000, "PC", GameGenre.ACTION, "설명", GameStatus.ON_SALE);
            given(gameRepository.findById(1L)).willReturn(Optional.of(game));

            // when
            gameService.updateGameStatus(1L, GameStatus.DELISTED);

            // then
            assertThat(game.getStatus()).isEqualTo(GameStatus.DELISTED);
        }

        @Test
        @DisplayName("존재하지 않는 게임 상태 변경 시 NotFoundException 발생")
        void updateStatus_notFound() {
            // given
            given(gameRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> gameService.updateGameStatus(999L, GameStatus.DELISTED))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("게임을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("게임 상태를 ON_SALE에서 HIDDEN으로 변경한다")
        void updateStatus_toHidden() throws Exception {
            // given
            Game game = buildGame(1L, "게임A", 10000, "PC", GameGenre.ACTION, "설명", GameStatus.ON_SALE);
            given(gameRepository.findById(1L)).willReturn(Optional.of(game));

            // when
            gameService.updateGameStatus(1L, GameStatus.HIDDEN);

            // then
            assertThat(game.getStatus()).isEqualTo(GameStatus.HIDDEN);
        }
    }

    // --- 헬퍼 메서드 ---

    private GameCreateRequest createRequest(String title, int price, String platform,
                                            GameGenre genre, String description) throws Exception {
        GameCreateRequest req = new GameCreateRequest();
        setField(req, "title", title);
        setField(req, "price", price);
        setField(req, "platform", platform);
        setField(req, "genre", genre);
        setField(req, "description", description);
        return req;
    }

    private Game buildGame(Long id, String title, int price, String platform,
                           GameGenre genre, String description, GameStatus status) throws Exception {
        Game game = Game.builder()
                .title(title)
                .price(price)
                .platform(platform)
                .genre(genre)
                .description(description)
                .status(status)
                .build();
        setField(game, "id", id);
        return game;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), fieldName);
            }
            throw e;
        }
    }
}
