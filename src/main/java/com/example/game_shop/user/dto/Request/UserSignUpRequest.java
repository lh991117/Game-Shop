package com.example.game_shop.user.dto.Request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor
public class UserSignUpRequest {
    
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2 ~ 20자 사이여야 합니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, max = 20, message = "비밀번호는 4~20자 사이여야 합니다.")
    private String password;
}
