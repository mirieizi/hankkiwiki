package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "식단 식사 종류 수정 요청 DTO")
public class DietUpdateMealTypeRequestDto {

    @NotBlank
    @Schema(description = "식단 ID", example = "1")
    private Long dietId;

    @NotBlank
    @Schema(description = "식사 종류", example = "LUNCH")
    private MealType mealType;
}
