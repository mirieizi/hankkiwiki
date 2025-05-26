package com.hankki.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "사용자 응답 DTO")
public class UserResponse {

    @NotBlank
    @Schema(description = "사용자 아이디", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "사용자 이메일", example = "test@example.com")
    private String email;

    @NotBlank
    @Schema(description = "닉네임", example = "테스트별명123")
    private String nickname;
}
