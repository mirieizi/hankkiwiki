package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.entity.Diet;
import com.hankki.domain.diet.entity.MealItem;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class DietResponseDto {

    private Long id;

    private String email;

    private LocalDate takeAt;

    private MealType mealType;

    private List<MealItem> mealItems;

    private String dietMemo;

    public DietResponseDto(Diet diet) {
        this.id = diet.getId();
        this.email = diet.getEmail();
        this.takeAt = diet.getTakeAt();
        this.mealType = diet.getMealType();
        this.mealItems = diet.getMealItems();
        this.dietMemo = diet.getDietMemo();
    }
}
