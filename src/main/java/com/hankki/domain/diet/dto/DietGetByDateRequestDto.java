package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DietGetByDateRequestDto {

    private String email;
    private String takeAt;
    private MealType mealType;
}
