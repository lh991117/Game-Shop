package com.example.game_shop.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game_shop.user.annotation.AuthUserId;
import com.example.game_shop.user.dto.Response.LibraryItemResponse;
import com.example.game_shop.user.service.LibraryService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/library")
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping("/me")
    public Page<LibraryItemResponse> myLibrary(@AuthUserId Long userId, Pageable pageable) {
        return libraryService.getMyLibrary(userId, pageable);
    }

}
