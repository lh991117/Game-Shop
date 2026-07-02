package com.example.game_shop.user.controller;

import com.example.game_shop.global.common.JwtTokenProvider;
import com.example.game_shop.security.CustomUserDetailsService;
import com.example.game_shop.user.dto.Request.UserLoginRequest;
import com.example.game_shop.user.dto.Request.UserSignUpRequest;
import com.example.game_shop.user.dto.Response.UserLoginResponse;
import com.example.game_shop.user.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthcontrollerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("POST /auth/signup - 회원가입")
    class SignUp {

        @Test
        @DisplayName("정상 회원가입 시 200 반환")
        @WithMockUser
        void signUp_success() throws Exception {
            // given
            UserSignUpRequest request = createSignUpRequest("test@test.com", "tester", "password123");
            doNothing().when(authService).signUp(any());

            // when & then
            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("이미 사용 중인 이메일이면 400 반환")
        @WithMockUser
        void signUp_duplicateEmail() throws Exception {
            // given
            UserSignUpRequest request = createSignUpRequest("dup@test.com", "tester", "password123");
            doThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다.")).when(authService).signUp(any());

            // when & then
            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/login - 로그인")
    class Login {

        @Test
        @DisplayName("정상 로그인 시 200 및 Access Token 반환")
        @WithMockUser
        void login_success() throws Exception {
            // given
            UserLoginRequest request = createLoginRequest("test@test.com", "password123");
            given(authService.login(any())).willReturn(new UserLoginResponse("mock.jwt.token"));

            // when & then
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"));
        }

        @Test
        @DisplayName("잘못된 인증 정보로 로그인 시 예외 발생")
        @WithMockUser
        void login_invalidCredentials() throws Exception {
            // given
            UserLoginRequest request = createLoginRequest("test@test.com", "wrongPassword");
            given(authService.login(any()))
                    .willThrow(new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

            // when & then
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // --- 헬퍼 메서드 ---

    private UserSignUpRequest createSignUpRequest(String email, String nickname, String password) throws Exception {
        UserSignUpRequest req = new UserSignUpRequest();
        setField(req, "email", email);
        setField(req, "nickname", nickname);
        setField(req, "password", password);
        return req;
    }

    private UserLoginRequest createLoginRequest(String email, String password) throws Exception {
        UserLoginRequest req = new UserLoginRequest();
        setField(req, "email", email);
        setField(req, "password", password);
        return req;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
