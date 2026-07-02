package com.example.game_shop.game.controller;

import com.example.game_shop.controller.TestSecurityConfig;
import com.example.game_shop.game.domain.GameGenre;
import com.example.game_shop.game.dto.response.GameResponse;
import com.example.game_shop.game.service.GameService;
import com.example.game_shop.global.common.AuthUserIdArgumentResolver;
import com.example.game_shop.global.common.JwtTokenProvider;
import com.example.game_shop.global.exception.NotFoundException;
import com.example.game_shop.security.CustomUserDetailsService;
import com.example.game_shop.user.dto.Response.PurchaseResponse;
import com.example.game_shop.user.service.PurchaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@Import(TestSecurityConfig.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private PurchaseService purchaseService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AuthUserIdArgumentResolver authUserIdArgumentResolver;

    @Nested
    @DisplayName("GET /games - 게임 목록 조회")
    class GetGames {

        @Test
        @DisplayName("비인증 사용자도 게임 목록을 조회할 수 있다")
        void getGames_anonymous_success() throws Exception {
            // given
            List<GameResponse> games = List.of(
                    buildGameResponse(1L, "엘든 링", 64800, GameGenre.RPG),
                    buildGameResponse(2L, "오버워치2", 0, GameGenre.FPS)
            );
            given(gameService.getGames(any(Pageable.class))).willReturn(new PageImpl<>(games));

            // when & then
            mockMvc.perform(get("/games"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].title").value("엘든 링"));
        }

        @Test
        @DisplayName("게임이 없으면 빈 목록이 반환된다")
        void getGames_empty() throws Exception {
            // given
            given(gameService.getGames(any(Pageable.class))).willReturn(new PageImpl<>(List.of()));

            // when & then
            mockMvc.perform(get("/games"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /games/{id} - 게임 상세 조회")
    class GetGame {

        @Test
        @DisplayName("존재하는 게임 조회 시 200 반환")
        void getGame_success() throws Exception {
            // given
            GameResponse response = buildGameResponse(1L, "엘든 링", 64800, GameGenre.RPG);
            given(gameService.getGame(1L)).willReturn(response);

            // when & then
            mockMvc.perform(get("/games/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("엘든 링"))
                    .andExpect(jsonPath("$.data.price").value(64800));
        }

        @Test
        @DisplayName("존재하지 않는 게임 조회 시 404 반환")
        void getGame_notFound() throws Exception {
            // given
            given(gameService.getGame(999L)).willThrow(new NotFoundException("게임을 찾을 수 없습니다. id=999"));

            // when & then
            mockMvc.perform(get("/games/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /games/search - 게임 검색")
    class SearchGames {

        @Test
        @DisplayName("검색 조건 없이 전체 조회 가능")
        void search_noCondition() throws Exception {
            // given
            given(gameService.searchForUser(any(), any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(
                            buildGameResponse(1L, "엘든 링", 64800, GameGenre.RPG)
                    )));

            // when & then
            mockMvc.perform(get("/games/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("엘든 링"));
        }

        @Test
        @DisplayName("키워드 파라미터로 검색 가능")
        void search_withKeyword() throws Exception {
            // given
            given(gameService.searchForUser(any(), any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of()));

            // when & then
            mockMvc.perform(get("/games/search")
                            .param("keyword", "엘든"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /games/{gameId}/purchase - 게임 구매")
    class Purchase {

        @Test
        @DisplayName("인증된 사용자는 게임을 구매할 수 있다")
        @WithMockUser
        void purchase_authenticated_success() throws Exception {
            // given
            PurchaseResponse response = PurchaseResponse.builder()
                    .gameId(1L)
                    .pricePaid(64800)
                    .purchasedAt(LocalDateTime.now())
                    .build();

            given(authUserIdArgumentResolver.supportsParameter(any())).willReturn(true);
            given(authUserIdArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(1L);
            given(purchaseService.purchase(any(), eq(1L))).willReturn(response);

            // when & then
            mockMvc.perform(post("/games/1/purchase")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.gameId").value(1))
                    .andExpect(jsonPath("$.pricePaid").value(64800));
        }

        @Test
        @DisplayName("비인증 사용자는 게임을 구매할 수 없다 (401)")
        void purchase_anonymous_unauthorized() throws Exception {
            // when & then
            mockMvc.perform(post("/games/1/purchase")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // --- 헬퍼 메서드 ---

    private GameResponse buildGameResponse(Long id, String title, int price, GameGenre genre) {
        return GameResponse.builder()
                .id(id)
                .title(title)
                .price(price)
                .platform("PC")
                .genre(genre)
                .description("설명")
                .build();
    }
}
