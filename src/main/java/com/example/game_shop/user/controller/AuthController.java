package com.example.game_shop.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game_shop.user.dto.Request.UserSignUpRequest;
import com.example.game_shop.user.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody UserSignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.ok().build();
    }

}
