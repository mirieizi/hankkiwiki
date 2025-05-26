package com.hankki.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileResponse {
    private String nickname;

    // (1) 기본 생성자 (NoArgsConstructor)
    public UserProfileResponse() {}

    // (2) 닉네임만 받는 생성자!
    public UserProfileResponse(String nickname) {
        this.nickname = nickname;
    }
}
