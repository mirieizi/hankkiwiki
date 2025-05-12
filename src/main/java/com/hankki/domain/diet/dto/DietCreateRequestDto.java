package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.entity.Diet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DietCreateRequestDto {

    private String email;
    private MealType mealType;
    private LocalDate takeAt;
    private String dietMemo;
    private List<Long> mealItemIds;

    public Diet toEntity() {
        return Diet.builder()
                .email(this.email)
                .takeAt(this.takeAt)
                .mealType(this.mealType)
                .dietMemo(this.dietMemo)
                .build();
    }
}
