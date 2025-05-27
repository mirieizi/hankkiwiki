package com.hankki.domain.diet.service;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;
import com.hankki.domain.diet.dto.GroupedDietResponseDto;
import com.hankki.domain.diet.entity.DietGroup;
import com.hankki.domain.food.entity.Food;

import java.time.LocalDate;
import java.util.List;

public interface DietService {

    void createDiet(Long userId, DietCreateRequestDto requestDto);

    List<DietGroup> getDietsByTakeAt(Long userId, LocalDate takeAt);

    List<DietGroup> getDietsByUserId(Long userId);

    void deleteDietByUserIdAndDietId(Long userId, Long dietId);

    void updateMealType(Long userId, Long dietId, MealType mealType);

    void updateTakeAt(Long userId, Long dietId, LocalDate takeAt);

    List<Long> findRecentFoodIdsByUserId(Long userId, LocalDate start, LocalDate end);

	boolean hasDietHistoryForRecentDays(Long userId, int days);

	List<GroupedDietResponseDto> getGroupedDietsByRecentDays(Long userId, int days);
	List<Food> findRecentFoods(Long userId, int days);

	

}
