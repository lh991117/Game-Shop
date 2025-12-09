package com.example.game_shop.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.game_shop.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
