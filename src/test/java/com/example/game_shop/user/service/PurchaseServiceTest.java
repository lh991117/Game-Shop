package com.example.game_shop.user.service;

import com.example.game_shop.game.domain.Game;
import com.example.game_shop.game.domain.GameGenre;
import com.example.game_shop.game.domain.GameStatus;
import com.example.game_shop.game.repository.GameRepository;
import com.example.game_shop.global.exception.BadRequestException;
import com.example.game_shop.global.exception.ConflictException;
import com.example.game_shop.global.exception.NotFoundException;
import com.example.game_shop.user.domain.UserGame;
import com.example.game_shop.user.dto.Response.PurchaseResponse;
import com.example.game_shop.user.repository.UserGameRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @InjectMocks
    private PurchaseService purchaseService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserGameRepository userGameRepository;

    @Nested
    @DisplayName("게임 구매")
    class Purchase {

        @Test
        @DisplayName("정상적으로 게임을 구매한다")
        void purchase_success() throws Exception {
            // given
            Long userId = 1L;
            Long gameId = 10L;
            Game game = buildGame(gameId, "엘든 링", 64800, GameStatus.ON_SALE);
            UserGame userGame = buildUserGame(1L, userId, game, 64800);

            given(gameRepository.findById(gameId)).willReturn(Optional.of(game));
            given(userGameRepository.existsByUserIdAndGame_Id(userId, gameId)).willReturn(false);
            given(userGameRepository.save(any(UserGame.class))).willReturn(userGame);

            // when
            PurchaseResponse response = purchaseService.purchase(userId, gameId);

            // then
            assertThat(response.getGameId()).isEqualTo(gameId);
            assertThat(response.getPricePaid()).isEqualTo(64800);
        }

        @Test
        @DisplayName("존재하지 않는 게임 구매 시 NotFoundException 발생")
        void purchase_gameNotFound() {
            // given
            given(gameRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> purchaseService.purchase(1L, 999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("게임을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("판매 중이 아닌 게임 구매 시 BadRequestException 발생")
        void purchase_notOnSale() throws Exception {
            // given
            Long gameId = 10L;
            Game game = buildGame(gameId, "단종 게임", 10000, GameStatus.DELISTED);
            given(gameRepository.findById(gameId)).willReturn(Optional.of(game));

            // when & then
            assertThatThrownBy(() -> purchaseService.purchase(1L, gameId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("판매 중인 게임만 구매할 수 있습니다.");

            verify(userGameRepository, never()).save(any());
        }

        @Test
        @DisplayName("HIDDEN 상태 게임 구매 시 BadRequestException 발생")
        void purchase_hiddenGame() throws Exception {
            // given
            Long gameId = 10L;
            Game game = buildGame(gameId, "숨김 게임", 10000, GameStatus.HIDDEN);
            given(gameRepository.findById(gameId)).willReturn(Optional.of(game));

            // when & then
            assertThatThrownBy(() -> purchaseService.purchase(1L, gameId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("판매 중인 게임만 구매할 수 있습니다.");
        }

        @Test
        @DisplayName("이미 구매한 게임 재구매 시 ConflictException 발생")
        void purchase_alreadyOwned() throws Exception {
            // given
            Long userId = 1L;
            Long gameId = 10L;
            Game game = buildGame(gameId, "엘든 링", 64800, GameStatus.ON_SALE);

            given(gameRepository.findById(gameId)).willReturn(Optional.of(game));
            given(userGameRepository.existsByUserIdAndGame_Id(userId, gameId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> purchaseService.purchase(userId, gameId))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("이미 구매한 게임입니다.");

            verify(userGameRepository, never()).save(any());
        }

        @Test
        @DisplayName("동시 요청으로 DB 유니크 제약 위반 시 ConflictException으로 변환된다")
        void purchase_dataIntegrityViolation_throwsConflict() throws Exception {
            // given
            Long userId = 1L;
            Long gameId = 10L;
            Game game = buildGame(gameId, "엘든 링", 64800, GameStatus.ON_SALE);

            given(gameRepository.findById(gameId)).willReturn(Optional.of(game));
            given(userGameRepository.existsByUserIdAndGame_Id(userId, gameId)).willReturn(false);
            given(userGameRepository.save(any(UserGame.class)))
                    .willThrow(new DataIntegrityViolationException("uk_user_game"));

            // when & then
            assertThatThrownBy(() -> purchaseService.purchase(userId, gameId))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("이미 구매한 게임입니다.");
        }

        @Test
        @DisplayName("구매 시 결제 금액은 게임 현재 가격과 동일하다")
        void purchase_pricePaidEqualsGamePrice() throws Exception {
            // given
            Long userId = 1L;
            Long gameId = 10L;
            int gamePrice = 49800;
            Game game = buildGame(gameId, "게임A", gamePrice, GameStatus.ON_SALE);
            UserGame userGame = buildUserGame(1L, userId, game, gamePrice);

            given(gameRepository.findById(gameId)).willReturn(Optional.of(game));
            given(userGameRepository.existsByUserIdAndGame_Id(userId, gameId)).willReturn(false);
            given(userGameRepository.save(any(UserGame.class))).willReturn(userGame);

            // when
            PurchaseResponse response = purchaseService.purchase(userId, gameId);

            // then
            assertThat(response.getPricePaid()).isEqualTo(gamePrice);
        }
    }

    // --- 헬퍼 메서드 ---

    private Game buildGame(Long id, String title, int price, GameStatus status) throws Exception {
        Game game = Game.builder()
                .title(title)
                .price(price)
                .platform("PC")
                .genre(GameGenre.RPG)
                .description("설명")
                .status(status)
                .build();
        setField(game, "id", id);
        return game;
    }

    private UserGame buildUserGame(Long id, Long userId, Game game, int pricePaid) throws Exception {
        UserGame ug = UserGame.of(userId, game, pricePaid);
        setField(ug, "id", id);
        return ug;
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
