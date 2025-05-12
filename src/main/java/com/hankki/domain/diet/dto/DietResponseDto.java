package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DietResponseDto {

    private Long id;
    private String email;
    private LocalDate takeAt;
    private MealType mealType;
    private String dietMemo;
    private List<MealItemPreviewResponseDto> mealItems;
}
