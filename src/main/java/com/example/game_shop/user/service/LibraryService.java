package com.example.game_shop.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game_shop.user.dto.Response.LibraryItemResponse;
import com.example.game_shop.user.repository.UserGameRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LibraryService {
    
    private final UserGameRepository userGameRepository;

    @Transactional(readOnly = true)
    public Page<LibraryItemResponse> getMyLibrary(Long userId, Pageable pageable){
        return userGameRepository.findMyLibrary(userId, pageable)
        .map(LibraryItemResponse::from);
    }

    @Transactional(readOnly = true)
    public boolean inOwned(Long userId, Long gameId){
        return userGameRepository.existsByUserIdAndGame_Id(userId, gameId);
    }
}
