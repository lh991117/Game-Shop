package com.example.game_shop.game.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game_shop.game.domain.Game;
import com.example.game_shop.game.domain.GameSearchCondition;
import com.example.game_shop.game.domain.GameSortType;
import com.example.game_shop.game.domain.GameStatus;
import com.example.game_shop.game.dto.request.GameCreateRequest;
import com.example.game_shop.game.dto.response.GameResponse;
import com.example.game_shop.game.repository.GameRepository;
import com.example.game_shop.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;

    // 게임 목록 추가
    @Transactional
    public GameResponse create(GameCreateRequest request) {
        Game game = Game.builder()
                .title(request.getTitle())
                .price(request.getPrice())
                .platform(request.getPlatform())
                .genre(request.getGenre())
                .description(request.getDescription())
                .status(GameStatus.ON_SALE)
                .build();

        Game saved = gameRepository.save(game);
        return GameResponse.from(saved);
    }

    // 게임 전체 조회
    public Page<GameResponse> getGames(Pageable pageable) {
        return gameRepository.findAllByStatus(GameStatus.ON_SALE, pageable)
                .map(GameResponse::from);
    }

    // 게임 전체 조회(관리자용)
    public Page<GameResponse> getGamesAdmin(Pageable pageable) {
        return gameRepository.findAll(pageable)
                .map(GameResponse::from);
    }

    public Page<GameResponse> searchForUser(GameSearchCondition condition, String sort, Pageable pageable) {
        GameSortType sortType = GameSortType.from(sort);

        return gameRepository.searchForUser(condition, sortType, pageable)
                .map(GameResponse::from);
    }

    public Page<GameResponse> searchForAdmin(GameSearchCondition condition, String sort, Pageable pageable) {
        GameSortType sortType = GameSortType.from(sort);

        return gameRepository.searchForAdmin(condition, sortType, pageable)
                .map(GameResponse::from);
    }

    public GameResponse getGame(Long id) {
        Game game = gameRepository.findByIdAndStatus(id, GameStatus.ON_SALE)
                .orElseThrow(() -> new NotFoundException("게임을 찾을 수 없습니다. id=" + id));

        return GameResponse.from(game);
    }

    @Transactional
    public void updateGameStatus(Long gameId, GameStatus status) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("게임을 찾을 수 없습니다."));

        game.changeStatus(status);
    }
}
