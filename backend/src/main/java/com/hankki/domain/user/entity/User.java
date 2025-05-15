package com.hankki.domain.user.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hankki.domain.user.constant.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long id;

    @Email
    @NotBlank
    @Column(name = "user_email", nullable = false, unique = true)
    private String email;

    @Size(min = 8, max = 20)
    @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).+$",
      message = "비밀번호는 8~20자, 영문+특수문자 필요"
    )
    @Column(name = "user_password", length = 72, nullable = false)
    private String password;

    @Size(max = 10)
    @Pattern(
      regexp = "^[A-Za-z0-9가-힣]+$",
      message = "닉네임은 10자 이하, 공백·특수문자 불가"
    )
    @Column(name = "user_nickname", nullable = false, unique = true)
    private String nickname;

    @OneToOne(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        optional = false
    )
    private UserHealthInfo healthInfo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    // autoApply가 true이므로 JPA가 알아서 RoleCoverter로 매핑해줌
    @Builder.Default
    private Set<Role> roles = EnumSet.of(Role.ROLE_USER);

    /** 이메일 변경 */
    public void changeEmail(String email) {
        this.email = email;
    }

    /** 비밀번호 변경 */
    public void changePassword(String rawPassword, PasswordEncoder encoder) {
        this.password = encoder.encode(rawPassword);
    }

    /** 닉네임 변경 */
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    /** 권한 리스트 반환 */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableSet(roles);
    }

    /** 인증에 사용할 username(email) 반환 */
    @Override
    public String getUsername() {
        return email;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return id != null && id.equals(u.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
