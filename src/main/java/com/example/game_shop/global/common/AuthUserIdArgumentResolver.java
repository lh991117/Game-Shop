package com.example.game_shop.global.common;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.access.method.P;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.example.game_shop.global.exception.UnauthorizedException;
import com.example.game_shop.security.CustomUserDetails;
import com.example.game_shop.user.annotation.AuthUserId;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUserId.class)
                && (Long.class.equals(parameter.getParameterType())) || long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof String)
            throw new UnauthorizedException("로그인이 필요합니다.");

        if (principal instanceof CustomUserDetails cud) {
            Long userId = cud.getUserId();
            if (userId == null) {
                throw new UnauthorizedException("유효하지 않은 인증 정보입니다.");
            }

            return userId;
        }

        throw new UnauthorizedException("유효하지 않은 사용자 정보입니다.");
    }

}
