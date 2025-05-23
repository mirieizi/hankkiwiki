package com.hankki.domain.food.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "음식의 간단한 응답 DTO")
public class FoodPreviewResponseDto {

    @NotBlank
    @Schema(description = "음식 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "음식 이름", example = "김치찌개")
    private String foodName;

    @NotBlank
    @Schema(description = "대분류", example = "밥류")
    private String majorCategory;

    @NotBlank
    @Schema(description = "1인분 기준 칼로리", example = "300")
    private double kcal;
}
