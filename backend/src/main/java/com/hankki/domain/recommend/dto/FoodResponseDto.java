package com.hankki.domain.recommend.dto;

import com.hankki.domain.food.entity.Food;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor  // ← 추가 (JSON 역직렬화용)
@Getter
@Schema(description = "음식 추천 응답 DTO")
public class FoodResponseDto {

    @NotNull  // ← @NotBlank → @NotNull 수정
    @Schema(description = "음식 ID", example = "1")
    private Long id;

    @Schema(description = "음식명", example = "김치찌개")
    private String foodName;

    @Schema(description = "대분류", example = "국・탕・찌개・전골류")
    private String majorCategory;

    @Schema(description = "소분류", example = "찌개류")
    private String subCategory;

    @Schema(description = "1회 제공량(g)", example = "250")
    private double servingSize;

    @Schema(description = "칼로리(kcal)", example = "120.5")
    private double kcal;

    @Schema(description = "수분(g)", example = "200.3")
    private double moisture;

    @Schema(description = "탄수화물(g)", example = "8.5")
    private double carbohydrate;

    @Schema(description = "단백질(g)", example = "7.2")
    private double protein;

    @Schema(description = "지방(g)", example = "6.8")
    private double fat;

    @Schema(description = "당류(g)", example = "3.2")
    private double sugar;

    @Schema(description = "나트륨(mg)", example = "850")
    private double sodium;

    @Schema(description = "콜레스테롤(mg)", example = "15")
    private double cholesterol;
    private String reason;

    public static FoodResponseDto fromEntity(Food food, String reason) {
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
                .reason(reason)
                .build();
    }

    // ✅ 호환성을 위한 별칭 메서드 추가
    public static FoodResponseDto from(Food food) {
        return fromEntity(food);
    }
}
