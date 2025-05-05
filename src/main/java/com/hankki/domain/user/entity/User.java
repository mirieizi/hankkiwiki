package com.hankki.domain.user.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	// UserDetails를 상속 받아 인증 객체로 사용함
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
    @Column(name = "user_password", nullable = false)
    private String password;

    @Size(max = 10)
    @Pattern(
      regexp = "^[A-Za-z0-9가-힣]+$",
      message = "닉네임은 10자 이하, 공백·특수문자 불가"
    )
    @Column(name = "user_nickname", nullable = false, unique = true)
    private String nickname;

    @OneToOne(mappedBy = "user",
              cascade = CascadeType.ALL,
              fetch = FetchType.LAZY,
              optional = false)
    private UserHealthInfo healthInfo;

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
    /** 그냥 equals를 하게 되면 object타입일 때 != User 타입일 때 */
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
    /** 권한 반환 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("user"));
	}
	/** 사용자의 id를 반환 (고유한 값) */
	@Override
	public String getUsername() {
		return email;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		// 만료되어 있는지 확인하는 로직
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		// 계정 잠금되어 있는지 확인하는 로직
		return true; // true: 잠금되지 않음
	}
	@Override
	public boolean isCredentialsNonExpired() {
		// 패스워드가 만료되었는지 확인하는 로직
		return true;
	}
	@Override
	public boolean isEnabled() {
		// 계정이 사용 가능한지 확인하는 로직
		return true; 
	}
}



