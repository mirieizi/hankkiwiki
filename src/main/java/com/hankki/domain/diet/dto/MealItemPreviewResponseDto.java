package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MajorCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MealItemPreviewResponseDto {
    private Long id;
    private String foodName;
    private MajorCategory majorCategory;
}
