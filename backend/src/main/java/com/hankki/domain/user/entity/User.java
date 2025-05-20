package com.hankki.domain.user.entity;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.hankki.domain.auth.constant.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
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
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long id;

    @Email
    @NotBlank
    @Column(name = "user_email", nullable = false, unique = true)
    private String email;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
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
}
