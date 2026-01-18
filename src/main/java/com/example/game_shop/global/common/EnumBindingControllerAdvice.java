package com.example.game_shop.global.common;

import java.beans.PropertyEditorSupport;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import com.example.game_shop.game.domain.GameGenre;

@ControllerAdvice
public class EnumBindingControllerAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(GameGenre.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(GameGenre.valueOf(text.toUpperCase()));
            }
        });
    }
}
