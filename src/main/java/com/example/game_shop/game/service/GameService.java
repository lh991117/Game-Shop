package com.example.game_shop.game.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game_shop.game.domain.Game;
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

    @Transactional
    public GameResponse create(GameCreateRequest request) {
        Game game = Game.builder()
                .title(request.getTitle())
                .price(request.getPrice())
                .platform(request.getPlatform())
                .genre(request.getGenre())
                .description(request.getDescription())
                .build();

        Game saved = gameRepository.save(game);
        return GameResponse.from(saved);
    }

    public List<GameResponse> getGames() {
        return gameRepository.findAll()
                .stream()
                .map(GameResponse::from)
                .toList();
    }

    public GameResponse getGame(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("게임을 찾을 수 없습니다. id=" + id));

        return GameResponse.from(game);
    }
}
