package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.entity.Diet;
import com.hankki.domain.diet.entity.MealItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DietCreateRequestDto {
    /**
     * TO-DO : 필수값을 null로 못 받게 하는 기능 필요
     */
    private String email;

    private MealType mealType;

    private LocalDate takeAt;

    private List<MealItem> mealItems;

    private String dietMemo;

    public Diet toEntity() {
        return Diet.builder()
                .email(this.email)
                .takeAt(this.takeAt)
                .mealType(this.mealType)
                .mealItems(this.mealItems)
                .dietMemo(this.dietMemo)
                .build();
    }
}
