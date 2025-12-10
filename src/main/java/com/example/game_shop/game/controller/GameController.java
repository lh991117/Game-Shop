package com.example.game_shop.game.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game_shop.game.dto.GameResponse;
import com.example.game_shop.game.service.GameService;
import com.example.game_shop.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    // 게임 목록
    @GetMapping
    public ApiResponse<List<GameResponse>> getGames() {
        return ApiResponse.ok(gameService.getGames());
    }

    // 게임 상세
    @GetMapping("/{id}")
    public ApiResponse<GameResponse> getGame(@PathVariable Long id) {
        return ApiResponse.ok(gameService.getGame(id));
    }
}
