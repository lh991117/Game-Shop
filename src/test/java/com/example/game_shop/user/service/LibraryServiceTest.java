package com.example.game_shop.user.service;

import com.example.game_shop.game.domain.Game;
import com.example.game_shop.game.domain.GameGenre;
import com.example.game_shop.game.domain.GameStatus;
import com.example.game_shop.user.domain.UserGame;
import com.example.game_shop.user.dto.Response.LibraryItemResponse;
import com.example.game_shop.user.repository.UserGameRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @InjectMocks
    private LibraryService libraryService;

    @Mock
    private UserGameRepository userGameRepository;

    @Nested
    @DisplayName("내 라이브러리 조회")
    class GetMyLibrary {

        @Test
        @DisplayName("구매한 게임 목록을 페이징하여 반환한다")
        void getMyLibrary_success() throws Exception {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            Game game1 = buildGame(1L, "엘든 링", "PC", GameGenre.RPG);
            Game game2 = buildGame(2L, "오버워치", "PC", GameGenre.FPS);
            UserGame ug1 = buildUserGame(1L, userId, game1, 64800);
            UserGame ug2 = buildUserGame(2L, userId, game2, 0);

            given(userGameRepository.findMyLibrary(userId, pageable))
                    .willReturn(new PageImpl<>(List.of(ug1, ug2)));

            // when
            Page<LibraryItemResponse> result = libraryService.getMyLibrary(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("엘든 링");
            assertThat(result.getContent().get(1).getTitle()).isEqualTo("오버워치");
        }

        @Test
        @DisplayName("구매한 게임이 없으면 빈 페이지를 반환한다")
        void getMyLibrary_empty() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            given(userGameRepository.findMyLibrary(userId, pageable))
                    .willReturn(new PageImpl<>(List.of()));

            // when
            Page<LibraryItemResponse> result = libraryService.getMyLibrary(userId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("LibraryItemResponse에 게임 정보가 올바르게 매핑된다")
        void getMyLibrary_responseMappedCorrectly() throws Exception {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            Game game = buildGame(1L, "엘든 링", "PC", GameGenre.RPG);
            UserGame ug = buildUserGame(1L, userId, game, 64800);

            given(userGameRepository.findMyLibrary(userId, pageable))
                    .willReturn(new PageImpl<>(List.of(ug)));

            // when
            Page<LibraryItemResponse> result = libraryService.getMyLibrary(userId, pageable);

            // then
            LibraryItemResponse item = result.getContent().get(0);
            assertThat(item.getGameId()).isEqualTo(1L);
            assertThat(item.getTitle()).isEqualTo("엘든 링");
            assertThat(item.getPlatform()).isEqualTo("PC");
            assertThat(item.getGenre()).isEqualTo(GameGenre.RPG);
            assertThat(item.getPurchasedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("게임 보유 여부 확인")
    class IsOwned {

        @Test
        @DisplayName("이미 구매한 게임이면 true를 반환한다")
        void isOwned_alreadyOwned() {
            // given
            given(userGameRepository.existsByUserIdAndGame_Id(1L, 10L)).willReturn(true);

            // when
            boolean result = libraryService.inOwned(1L, 10L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("구매하지 않은 게임이면 false를 반환한다")
        void isOwned_notOwned() {
            // given
            given(userGameRepository.existsByUserIdAndGame_Id(1L, 99L)).willReturn(false);

            // when
            boolean result = libraryService.inOwned(1L, 99L);

            // then
            assertThat(result).isFalse();
        }
    }

    // --- 헬퍼 메서드 ---

    private Game buildGame(Long id, String title, String platform, GameGenre genre) throws Exception {
        Game game = Game.builder()
                .title(title)
                .price(64800)
                .platform(platform)
                .genre(genre)
                .description("설명")
                .status(GameStatus.ON_SALE)
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
