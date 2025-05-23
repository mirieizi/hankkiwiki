package com.hankki.domain.diet.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.dto.*;
import com.hankki.domain.diet.entity.DietGroup;
import com.hankki.domain.diet.entity.DietFood;
import com.hankki.domain.diet.repository.DietFoodRepository;
import com.hankki.domain.diet.repository.DietGroupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DietServiceImpl implements DietService {

    private final DietGroupRepository dietGroupRepository;
    private final DietFoodRepository dietFoodRepository;

    @Override
    public void createDiet(Long userId, DietCreateRequestDto requestDto) {
        for (DietCreateRequestDto.MealWithFoods meal : requestDto.getFoods()) {
            try {
                dietOneMeal(userId, requestDto.getTakeAt(), meal);
            } catch (Exception e) {
                log.error("[DietService] {} 식사 생성 중 오류 발생: {}", meal.getMealType(), e.getMessage());
            }
        }
    }

    @Transactional
    public void dietOneMeal(Long userId, LocalDate takeAt, DietCreateRequestDto.MealWithFoods meal) {
        DietGroup existingDietGroup = dietGroupRepository.findByUserIdAndTakeAtAndMealType(userId, takeAt, meal.getMealType());

        if (existingDietGroup != null) {
            for (Long foodId : meal.getFoodIds()) {
                if (!dietFoodRepository.existsByDietGroupIdAndFoodId(existingDietGroup.getId(), foodId)) {
                    DietFood dietFood = DietFood.builder()
                            .dietGroupId(existingDietGroup.getId())
                            .foodId(foodId)
                            .build();
                    dietFoodRepository.save(dietFood);
                }
            }
            log.info("[DietService] {} 식사에 기존 식단에 음식 추가 완료", meal.getMealType());
            return;
        }

        DietGroup createdDietGroup = dietGroupRepository.save(
                DietGroup.builder()
                        .userId(userId)
                        .takeAt(takeAt)
                        .mealType(meal.getMealType())
                        .build()
        );

        for (Long foodId : meal.getFoodIds()) {
            DietFood dietFood = DietFood.builder()
                    .dietGroupId(createdDietGroup.getId())
                    .foodId(foodId)
                    .build();
            dietFoodRepository.save(dietFood);
        }

        log.info("[DietService] {} 식사 저장 완료", meal.getMealType());
    }


    @Override
    @Transactional
    public List<DietGroup> getDietsByTakeAt(Long userId, LocalDate takeAt) {
        return dietGroupRepository.findDietsByUserIdAndTakeAt(userId, takeAt);
    }

    @Override
    @Transactional
    public List<DietGroup> getDietsByUserId(Long userId) {
        return dietGroupRepository.findDietsByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteDietByUserIdAndDietId(Long userId, Long dietGroupId) {
        DietGroup dietGroup = dietGroupRepository.findById(dietGroupId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!dietGroup.getUserId().equals(userId)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }

        dietFoodRepository.deleteByDietGroupId(dietGroupId);
        dietGroupRepository.deleteById(dietGroupId);
        log.info("[DietService] DietGroup 삭제 성공: {}", dietGroupId);
    }

    @Override
    @Transactional
    public void updateMealType(Long userId, Long dietGroupId, MealType mealType) {
        DietGroup dietGroup = dietGroupRepository.findById(dietGroupId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!dietGroup.getUserId().equals(userId)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }

        dietGroup.setMealType(mealType);
        dietGroupRepository.save(dietGroup);
        log.info("[DietService] DietGroup MealType {}(으)로 수정 성공", mealType.name());
    }

    @Override
    @Transactional
    public void updateTakeAt(Long userId, Long dietGroupId, LocalDate takeAt) {
        DietGroup dietGroup = dietGroupRepository.findById(dietGroupId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!dietGroup.getUserId().equals(userId)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }

        dietGroup.setTakeAt(takeAt);
        dietGroupRepository.save(dietGroup);
        log.info("[DietService] DietGroup takeAt {}(으)로 수정 성공", takeAt);
    }

    @Override
    public List<Long> findRecentFoodIdsByUserId(Long userId, LocalDate start, LocalDate end) {
        List<Long> groupIds = dietGroupRepository.findIdsByUserIdAndTakeAtBetween(userId, start, end);
        if (groupIds.isEmpty()) return List.of();

        return dietFoodRepository.findFoodIdsByDietGroupIdIn(groupIds);
    }

}
