package com.hankki.domain.food.dto;

import com.hankki.domain.food.constant.MajorCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FoodPreviewResponseDto {
    private Long id;
    private String foodName;
    private MajorCategory majorCategory;
    private int kcal;
}
