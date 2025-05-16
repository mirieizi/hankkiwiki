package com.hankki.domain.auth.dto;

import java.io.Serializable;
import com.hankki.domain.user.entity.User;

public class UserPrincipal implements Serializable {
    private final Long userId;
    private final String email;
    private final String nickname;

    public UserPrincipal(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user);
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }
}
