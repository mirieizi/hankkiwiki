package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DietResponseDto {
    private Long id;
    private LocalDate takeAt;
    private MealType mealType;
    private List<FoodPreviewResponseDto> foods;
}
