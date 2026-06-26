package com.example.game_shop.game.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game_shop.game.domain.GameSearchCondition;
import com.example.game_shop.game.dto.request.GameCreateRequest;
import com.example.game_shop.game.dto.request.GameStatusUpdateRequest;
import com.example.game_shop.game.dto.response.GameResponse;
import com.example.game_shop.game.service.GameService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/games")
public class AdminGameController {

    private final GameService gameService;

    // 게임 등록
    @PostMapping("/create")
    public ResponseEntity<GameResponse> create(@Valid @RequestBody GameCreateRequest request) {
        return ResponseEntity.ok(gameService.create(request));
    }

    // 게임 상태 값 변경
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid GameStatusUpdateRequest request) {
        gameService.updateGameStatus(id, request.getStatus());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Page<GameResponse> getGamesAdmin(Pageable pageable) {
        return gameService.getGamesAdmin(pageable);
    }

    @GetMapping("/search")
    public Page<GameResponse> searchAdmin(
            GameSearchCondition condition,
            @RequestParam(required = false) String sort,
            Pageable pageable) {
        return gameService.searchForAdmin(condition, sort, pageable);
    }

}
