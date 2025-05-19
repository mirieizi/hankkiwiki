package com.hankki.domain.diet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "식사일자에 따른 사용자의 식단 요청 DTO")
public class DietGetByTakeAtRequestDto {

    @NotBlank
    @Schema(description = "사용자 이메일", example = "test@example.com")
    private String email;

    @NotBlank
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "식사 일자", example = "2025-05-28")
    private LocalDate takeAt;
}
