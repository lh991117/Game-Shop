package com.example.game_shop.game.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game_shop.game.dto.request.GameCreateRequest;
import com.example.game_shop.game.dto.request.GameStatusUpdateRequest;
import com.example.game_shop.game.dto.response.GameResponse;
import com.example.game_shop.game.service.GameService;
import com.example.game_shop.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    // 게임 등록(ADMIN만)
    @PostMapping("/create")
    public ResponseEntity<GameResponse> create(@RequestBody GameCreateRequest request) {
        return ResponseEntity.ok(gameService.create(request));
    }

    // 게임 상태값 변경(ADMIN만)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
        @PathVariable Long id,
        @RequestBody @Valid GameStatusUpdateRequest request
    ) {
        gameService.updateGameStatus(id, request.getStatus());
        return ResponseEntity.noContent().build();
    }

    // 게임 목록
    @GetMapping
    public Page<GameResponse> getGames(Pageable pageable) {
        return gameService.getGames(pageable);
    }

    // 게임 상세
    @GetMapping("/{id}")
    public ApiResponse<GameResponse> getGame(@PathVariable Long id) {
        return ApiResponse.ok(gameService.getGame(id));
    }
}
