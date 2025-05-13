package com.hankki.domain.user.dto;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.hankki.domain.user.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateUserRequest {

    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수 입력입니다.")
    private String email;

    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).+$",
      message = "비밀번호는 영문자와 특수문자를 각각 최소 1개 이상 포함해야 합니다."
    )
    private String password;

    @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다.")
    @Pattern(
      regexp = "^[A-Za-z0-9가-힣]+$",
      message = "닉네임에 공백이나 특수문자는 사용할 수 없습니다."
    )
    private String nickname;

    public User toUser(PasswordEncoder encoder) {
        return User.builder()
            .email(this.email)
            .password(encoder.encode(this.password))
            .nickname(this.nickname)
            .build();
    }
}