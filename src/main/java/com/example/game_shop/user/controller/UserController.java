package com.example.game_shop.user.controller;

import com.example.game_shop.global.common.ApiResponse;
import com.example.game_shop.user.dto.Request.UserSignUpRequest;
import com.example.game_shop.user.dto.Response.UserResponse;
import com.example.game_shop.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ApiResponse<UserResponse> signUp(@RequestBody @Valid UserSignUpRequest request) {
        return ApiResponse.ok(userService.signUp(request));
    }

    // 유저 단건 조회
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUser(id));
    }
}
