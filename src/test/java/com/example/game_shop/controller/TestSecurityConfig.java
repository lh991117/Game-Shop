package com.example.game_shop.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * @WebMvcTest 전용 Security 설정.
 * 실제 SecurityConfig 대신 이 설정을 @Import해서 사용한다.
 * - JwtAuthenticationFilter 등록 없음 (필터 없이 순수 권한 규칙만 검증)
 * - 실제 SecurityConfig의 인가 규칙을 그대로 반영
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/signup", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/games/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/games/*/purchase").authenticated()
                        .requestMatchers("/library/**").authenticated()
                        .requestMatchers("/admin/games/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                // 미인증 사용자 접근 시 403 대신 401 반환
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );;

        return http.build();
    }
}
