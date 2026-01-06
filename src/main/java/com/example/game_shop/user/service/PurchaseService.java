package com.example.game_shop.user.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game_shop.game.domain.Game;
import com.example.game_shop.game.domain.GameStatus;
import com.example.game_shop.game.repository.GameRepository;
import com.example.game_shop.global.exception.BadRequestException;
import com.example.game_shop.global.exception.ConflictException;
import com.example.game_shop.global.exception.NotFoundException;
import com.example.game_shop.user.domain.UserGame;
import com.example.game_shop.user.dto.Response.PurchaseResponse;
import com.example.game_shop.user.repository.UserGameRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    
    @Transactional
    public PurchaseResponse purchase(Long userId, Long gameId){
        Game game = gameRepository.findById(gameId)
        .orElseThrow(()-> new NotFoundException("게임을 찾을 수 없습니다."));

        if(game.getStatus() != GameStatus.ON_SALE){
            throw new BadRequestException("판매 중인 게임만 구매할 수 있습니다.");
        }

        if(userGameRepository.existsByUserIdAndGame_Id(userId, gameId)){
            throw new ConflictException("이미 구매한 게임입니다.");
        }

        try{
            UserGame userGame = userGameRepository.save(UserGame.of(userId, game, game.getPrice()));
            return PurchaseResponse.from(userGame);
        }catch(DataIntegrityViolationException e){
            throw new ConflictException("이미 구매한 게임입니다.");
        }
    }
}
