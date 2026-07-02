package com.example.game_shop.user.service;

import com.example.game_shop.global.common.JwtTokenProvider;
import com.example.game_shop.user.domain.User;
import com.example.game_shop.user.domain.UserRole;
import com.example.game_shop.user.dto.Request.UserLoginRequest;
import com.example.game_shop.user.dto.Request.UserSignUpRequest;
import com.example.game_shop.user.dto.Response.UserLoginResponse;
import com.example.game_shop.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("회원가입")
    class SignUp{

        @Test
        @DisplayName("정상 회원가입 성공")
        void signUp_success() {
            // given
            UserSignUpRequest request = createSignUpRequest("test@test.com", "tester", "password1234");
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

            // when & then
            authService.signUp(request); // 예외 없이 통과하면 성공
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 시 예외 발생")
        void signUp_duplicateEmail_throwsException() {
            // given
            UserSignUpRequest request = createSignUpRequest("duplicate@test.com", "tester", "password1234");
            given(userRepository.existsByEmail("duplicate@test.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 사용 중인 이메일입니다.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("비밀번호가 인코딩되어 저장된다")
        void signUp_passwordEncoded() {
            // given
            UserSignUpRequest request = createSignUpRequest("test@test.com", "tester", "rawPassword");
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode("rawPassword")).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            authService.signUp(request);

            // then
            verify(passwordEncoder).encode("rawPassword");
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("정상 로그인 시 Access Token 반환")
        void login_success() {
            // given
            UserLoginRequest request = createLoginRequest("test@test.com", "password1234");

            User user = createUser(1L, "test@test.com", "tester", UserRole.ROLE_USER);
            Authentication auth = mock(Authentication.class);

            given(authenticationManager.authenticate(any())).willReturn(auth);
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(jwtTokenProvider.createAccessToken(any(), anyString(), any())).willReturn("mock.jwt.token");

            // when
            UserLoginResponse response = authService.login(request);

            // then
            assertThat(response.getAccessToken()).isEqualTo("mock.jwt.token");
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
        void login_wrongPassword_throwsException() {
            // given
            UserLoginRequest request = createLoginRequest("test@test.com", "wrongPassword");
            given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("Bad credentials"));

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
        void login_userNotFound_throwsException() {
            // given
            UserLoginRequest request = createLoginRequest("notfound@test.com", "password");
            Authentication auth = mock(Authentication.class);

            given(authenticationManager.authenticate(any())).willReturn(auth);
            given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유저가 존재하지 않습니다.");
        }
    }

    // --- 헬퍼 메서드 ---

    private UserSignUpRequest createSignUpRequest(String email, String nickname, String password) {
        try {
            UserSignUpRequest req = new UserSignUpRequest();
            setField(req, "email", email);
            setField(req, "nickname", nickname);
            setField(req, "password", password);
            return req;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private UserLoginRequest createLoginRequest(String email, String password) {
        try {
            UserLoginRequest req = new UserLoginRequest();
            setField(req, "email", email);
            setField(req, "password", password);
            return req;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User createUser(Long id, String email, String nickname, UserRole role) {
        try {
            User user = User.builder()
                    .email(email)
                    .nickname(nickname)
                    .password("encodedPassword")
                    .role(role)
                    .build();
            setField(user, "id", id);
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
