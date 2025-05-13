package com.hankki.domain.food.dto;

import com.hankki.domain.diet.constant.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FoodGroupDto {
    private MealType mealType;
    private List<FoodPreviewResponseDto> foods;
}
