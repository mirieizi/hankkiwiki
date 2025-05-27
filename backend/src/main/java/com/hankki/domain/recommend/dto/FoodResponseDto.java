package com.hankki.domain.recommend.dto;

import com.hankki.domain.food.entity.Food;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
@Schema(description = "음식 추천 응답 DTO", implementation = FoodResponseDto.class)
public class FoodResponseDto {

    @NotBlank
    private Long id;
    private String foodName;
    private String majorCategory;
    private String subCategory;
    private double servingSize;
    private double kcal;
    private double moisture;
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
                .servingSize(food.getServingSize())
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
