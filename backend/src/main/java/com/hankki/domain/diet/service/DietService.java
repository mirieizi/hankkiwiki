package com.hankki.domain.diet.service;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.entity.Diet;

import java.time.LocalDate;
import java.util.List;

public interface DietService {

    void createDiet(Long userId, DietCreateRequestDto requestDto);

    List<Diet> getDietsByTakeAt(Long userId, LocalDate takeAt);

    List<Diet> getDietsByUserId(Long userId);

    void deleteDietByUserIdAndDietId(Long userId, Long dietId);

    void updateMealType(Long userId, Long dietId, MealType mealType);

    void updateTakeAt(Long userId, Long dietId, LocalDate takeAt);
}
