package com.example.game_shop.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.game_shop.user.domain.UserGame;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {

    boolean existsByUserIdAndGame_Id(Long userId, Long gameId);

    Optional<UserGame> findByUserIdAndGame_Id(Long userId, Long gameId);

    @Query("""
            select ug from UserGame ug
            join fetch ug.game g
            where ug.userId = :userId
            """)
    Page<UserGame> findMyLibrary(Long userId, Pageable pageable);
}
