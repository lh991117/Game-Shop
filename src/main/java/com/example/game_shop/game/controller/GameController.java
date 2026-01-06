package com.example.game_shop.game.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game_shop.game.dto.request.GameCreateRequest;
import com.example.game_shop.game.dto.request.GameStatusUpdateRequest;
import com.example.game_shop.game.dto.response.GameResponse;
import com.example.game_shop.game.service.GameService;
import com.example.game_shop.global.common.ApiResponse;
import com.example.game_shop.user.annotation.AuthUserId;
import com.example.game_shop.user.dto.Response.PurchaseResponse;
import com.example.game_shop.user.service.PurchaseService;

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
    private final PurchaseService purchaseService;

    @PostMapping("/{gameId}/purchase")
    public PurchaseResponse purchase(@AuthUserId Long userId, @PathVariable Long gameId) {
        return purchaseService.purchase(userId, gameId);
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
