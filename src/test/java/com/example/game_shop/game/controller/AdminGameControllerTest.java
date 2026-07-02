package com.example.game_shop.game.controller;

import com.example.game_shop.controller.TestSecurityConfig;
import com.example.game_shop.game.domain.GameGenre;
import com.example.game_shop.game.dto.response.GameResponse;
import com.example.game_shop.game.service.GameService;
import com.example.game_shop.global.common.JwtTokenProvider;
import com.example.game_shop.global.exception.NotFoundException;
import com.example.game_shop.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminGameController.class)
@Import(TestSecurityConfig.class)
class AdminGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("POST /admin/games/create - 게임 등록")
    class Create {

        @Test
        @DisplayName("관리자는 게임을 등록할 수 있다")
        @WithMockUser(authorities = "ROLE_ADMIN")
        void create_admin_success() throws Exception {
            // given
            Map<String, Object> request = Map.of(
                    "title", "엘든 링",
                    "price", 64800,
                    "platform", "PC",
                    "genre", "RPG",
                    "description", "오픈월드 RPG"
            );
            GameResponse response = buildGameResponse(1L, "엘든 링", 64800, GameGenre.RPG);
            given(gameService.create(any())).willReturn(response);

            // when & then
            mockMvc.perform(post("/admin/games/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("엘든 링"))
                    .andExpect(jsonPath("$.price").value(64800));
        }

        @Test
        @DisplayName("일반 사용자는 게임 등록 API에 접근 불가 (403)")
        @WithMockUser(authorities = "ROLE_USER")
        void create_user_forbidden() throws Exception {
            // given
            Map<String, Object> request = Map.of(
                    "title", "엘든 링",
                    "price", 64800,
                    "platform", "PC",
                    "genre", "RPG",
                    "description", "설명"
            );

            // when & then
            mockMvc.perform(post("/admin/games/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("제목이 없으면 400 반환")
        @WithMockUser(authorities = "ROLE_ADMIN")
        void create_missingTitle_badRequest() throws Exception {
            // given
            Map<String, Object> request = Map.of(
                    "title", "",
                    "price", 64800,
                    "platform", "PC",
                    "genre", "RPG",
                    "description", "설명"
            );

            // when & then
            mockMvc.perform(post("/admin/games/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("가격이 음수이면 400 반환")
        @WithMockUser(authorities = "ROLE_ADMIN")
        void create_negativePrice_badRequest() throws Exception {
            // given
            Map<String, Object> request = Map.of(
                    "title", "테스트 게임",
                    "price", -1000,
                    "platform", "PC",
                    "genre", "RPG",
                    "description", "설명"
            );

            // when & then
            mockMvc.perform(post("/admin/games/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /admin/games/{id}/status - 게임 상태 변경")
    class UpdateStatus {

        @Test
        @DisplayName("관리자는 게임 상태를 변경할 수 있다")
        @WithMockUser(authorities = "ROLE_ADMIN")
        void updateStatus_admin_success() throws Exception {
            // given
            Map<String, String> request = Map.of("status", "DELISTED");
            doNothing().when(gameService).updateGameStatus(eq(1L), any());

            // when & then
            mockMvc.perform(patch("/admin/games/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("일반 사용자는 게임 상태 변경 불가 (403)")
        @WithMockUser(authorities = "ROLE_USER")
        void updateStatus_user_forbidden() throws Exception {
            // given
            Map<String, String> request = Map.of("status", "DELISTED");

            // when & then
            mockMvc.perform(patch("/admin/games/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 게임 상태 변경 시 404 반환")
        @WithMockUser(authorities = "ROLE_ADMIN")
        void updateStatus_notFound() throws Exception {
            // given
            Map<String, String> request = Map.of("status", "DELISTED");
            doThrow(new NotFoundException("게임을 찾을 수 없습니다."))
                    .when(gameService).updateGameStatus(eq(999L), any());

            // when & then
            mockMvc.perform(patch("/admin/games/999/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /admin/games - 관리자용 게임 목록 조회")
    class GetGamesAdmin {

        @Test
        @DisplayName("관리자는 전체 게임 목록을 조회할 수 있다")
        @WithMockUser(authorities = "ROLE_ADMIN")
        void getGamesAdmin_success() throws Exception {
            // given
            List<GameResponse> games = List.of(
                    buildGameResponse(1L, "엘든 링", 64800, GameGenre.RPG),
                    buildGameResponse(2L, "숨겨진 게임", 10000, GameGenre.ACTION)
            );
            given(gameService.getGamesAdmin(any(Pageable.class))).willReturn(new PageImpl<>(games));

            // when & then
            mockMvc.perform(get("/admin/games"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2));
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
