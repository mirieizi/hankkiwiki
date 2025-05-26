package com.hankki.domain.auth.dto;

import java.io.Serializable;

import com.hankki.domain.user.constant.Gender;
import com.hankki.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserPrincipal implements Serializable {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final Gender gender;

    public UserPrincipal(User user, Gender gender) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.gender = gender;
    }

    public static UserPrincipal from(User user, Gender gender) {
        return new UserPrincipal(user, gender);
    }

}
