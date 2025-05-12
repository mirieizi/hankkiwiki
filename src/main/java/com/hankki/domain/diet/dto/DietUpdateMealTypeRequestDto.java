package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DietUpdateMealTypeRequestDto {
    @NotBlank
    private Long dietId;

    @NotBlank
    private MealType mealType;
}
