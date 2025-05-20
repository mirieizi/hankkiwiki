package com.hankki.domain.auth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.auth.dto.UserPrincipal;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthUser implements UserDetails {
    private final User user;

    public AuthUser(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
            .map(role -> (GrantedAuthority) () -> role.name())
            .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 직접 User를 반환하지 않고 UserPrincipal로 감싸서 반환
     */
    public UserPrincipal getUserPrincipal() {
        return new UserPrincipal(this.user);
    }

    public User getUser() {
        return this.user;
    }

}
