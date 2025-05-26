package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Schema(description = "식사 응답 DTO")
public class DietResponseDto {

    @NotBlank
    @Schema(description = "식단 ID", example = "1")
    private Long id;

    @Schema(description = "식사 일자", example = "2025-05-28")
    private LocalDate takeAt;

    @Schema(description = "식사 종류", example = "LUNCH")
    private MealType mealType;

    @Schema(description = "음식 간편 조회 리스트", implementation = FoodPreviewResponseDto.class)
    private List<FoodPreviewResponseDto> foods;
}
