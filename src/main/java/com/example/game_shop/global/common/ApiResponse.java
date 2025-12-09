package com.example.game_shop.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;

    public static <T> ApiResponse ok(T data) {
        return new ApiResponse<T>(true, data, null);
    }

    public static <T> ApiResponse fail(String message) {
        return new ApiResponse<T>(false, null, message);
    }
}
