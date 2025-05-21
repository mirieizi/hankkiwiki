package com.hankki.domain.auth.entity;

import com.hankki.domain.user.constant.Gender;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.auth.dto.UserPrincipal;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class AuthUser implements UserDetails {

    private final User user;
    private final Gender gender;

    public AuthUser(User user, Gender gender) {
        this.user = user;
        this.gender = gender;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRoles() == null) {
            throw new IllegalStateException("User roles must not be null");
        }

        return user.getRoles().stream()
            .filter(role -> role != null)
            .map(role -> (GrantedAuthority) () -> "ROLE_" + role.name())  // 권한 prefix 명시
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
        return new UserPrincipal(this.user, this.gender);
    }

}
