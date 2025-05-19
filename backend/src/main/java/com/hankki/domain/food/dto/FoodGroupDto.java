package com.hankki.domain.food.dto;

import com.hankki.domain.diet.constant.MealType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "음식 선택 시 보여지는 간단한 음식 정보 응답 DTO")
public class FoodGroupDto {

    @NotBlank
    @Schema(description = "식사 종류", example = "LUNCH")
    private MealType mealType;

    @NotBlank
    @Schema(description = "음식의 간단한 응답 리스트", implementation = FoodPreviewResponseDto.class)
    private List<FoodPreviewResponseDto> foods;
}
