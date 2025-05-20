package com.hankki.domain.recommend.dto;

import com.hankki.domain.food.entity.Food;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class FoodResponseDto {
    private Long id;
    private String foodName;
    private String majorCategory;
    private String subCategory;
    private int amountStandard;
    private int kcal;
    private int moisture;
    private double carbohydrate;
    private double protein;
    private double fat;
    private double sugar;
    private double sodium;
    private double cholesterol;

    public static FoodResponseDto fromEntity(Food food) {
        return FoodResponseDto.builder()
                .id(food.getId())
                .foodName(food.getFoodName())
                .majorCategory(food.getMajorCategory())
                .subCategory(food.getSubCategory())
                .amountStandard(food.getAmountStandard())
                .kcal(food.getKcal())
                .moisture(food.getMoisture())
                .carbohydrate(food.getCarbohydrate())
                .protein(food.getProtein())
                .fat(food.getFat())
                .sugar(food.getSugar())
                .sodium(food.getSodium())
                .cholesterol(food.getCholesterol())
                .build();
    }
}
