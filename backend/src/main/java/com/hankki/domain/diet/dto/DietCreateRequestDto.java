package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "식단 생성 요청 DTO")
public class DietCreateRequestDto {

    @NotBlank
    @Schema(description = "식사 일자", example = "2025-05-28")
    private LocalDate takeAt;

    @NotBlank
    @Schema(description = "식사 정보와 음식들", implementation = MealWithFoods.class)
    private List<MealWithFoods> foods;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealWithFoods {
        private MealType mealType;
        private List<Long> foodIds;
    }
}
