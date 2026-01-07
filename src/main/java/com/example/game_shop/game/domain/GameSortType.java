package com.example.game_shop.game.domain;

public enum GameSortType {
    LATEST,
    PRICE_ASC,
    PRICE_DESC;

    public static GameSortType from(String sort) {
        if (sort == null || sort.isBlank())
            return LATEST;
        return switch (sort.toLowerCase()) {
            case "latest" -> LATEST;
            case "pricesasc", "price_asc" -> PRICE_ASC;
            case "pricedesc", "price_desc" -> PRICE_DESC;
            default -> LATEST;
        };
    }
}
