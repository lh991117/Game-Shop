package com.example.game_shop.user.dto.Response;

import java.time.LocalDateTime;

import com.example.game_shop.user.domain.UserGame;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseResponse {

    private Long gameId;
    private int pricePaid;
    private LocalDateTime purchasedAt;

    public static PurchaseResponse from(UserGame ug) {
        return PurchaseResponse.builder()
                .gameId(ug.getGame().getId())
                .pricePaid(ug.getPricePaid())
                .purchasedAt(ug.getPurchasedAt())
                .build();
    }
}
