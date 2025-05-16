package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Schema(description = "[ADMIN] 식단 수정 요청 DTO")
public class DietUpdateRequestDto {

    @NotBlank
    @Schema(description = "식단 ID", example = "1")
    private Long dietId;

    @NotBlank
    @Schema(description = "사용자 이메일", example = "test@example.com")
    private String email;

    @Schema(description = "식사 일자", example = "2025-05-28")
    private LocalDate takeAt;

    @Schema(description = "식사 종류", example = "LUNCH")
    private MealType mealType;

}
