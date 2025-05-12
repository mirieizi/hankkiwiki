package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DietUpdateInfoRequestDto {
    private Long dietId;
    private String email;
    private MealType mealType;
    private String dietMemo;
}
