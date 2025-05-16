package com.hankki.domain.diet.service;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.entity.Diet;

import java.time.LocalDate;
import java.util.List;

public interface DietService {

    void createDiet(String email, DietCreateRequestDto requestDto);

    List<Diet> getDietsByTakeAt(String email, LocalDate takeAt);

    List<Diet> getDietsByEmail(String email);

    void deleteDietById(String email, Long dietId);

    void updateMealType(String email, Long dietId, MealType mealType);
}
