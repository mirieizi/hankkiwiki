package com.hankki.domain.diet.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.dto.*;
import com.hankki.domain.diet.entity.Diet;
import com.hankki.domain.recommend.entity.UserDietFoodMap;
import com.hankki.domain.recommend.mapper.UserDietFoodMapper;
import com.hankki.domain.diet.repository.DietRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DietServiceImpl implements DietService {

    private final DietRepository dietRepository;
    private final UserDietFoodMapper userDietFoodMapper;

    @Override
    public void createDiet(Long userId, DietCreateRequestDto requestDto) {
        for (DietCreateRequestDto.MealWithFoods meal : requestDto.getFoods()) {
            try {
                dietOneMeal(userId, requestDto.getTakeAt(), meal); // 내부에서 트랜잭션
            } catch (Exception e) {
                log.error("[DietService] {} 식사 생성 중 오류 발생: {}", meal.getMealType(), e.getMessage());
            }
        }
    }

    @Transactional
    private void dietOneMeal(Long userId, LocalDate takeAt, DietCreateRequestDto.MealWithFoods meal) {
        if (dietRepository.existsByUserIdAndTakeAtAndMealType(userId, takeAt, meal.getMealType())) {
            log.warn("[DietService] 이미 존재하는 식단 - userId: {}, takeAt: {}, mealType: {}", userId, takeAt, meal.getMealType());
            throw new HankkiWikiException(ExceptionStatus.INVALID_DIET_INPUT);
        }

        Diet createdDiet = dietRepository.save(
                Diet.builder()
                        .userId(userId)
                        .takeAt(takeAt)
                        .mealType(meal.getMealType())
                        .build()
        );

        for (Long foodId : meal.getFoodIds()) {
            UserDietFoodMap map = UserDietFoodMap.builder()
                    .userId(userId)
                    .dietId(createdDiet.getId())
                    .foodId(foodId)
                    .build();
            userDietFoodMapper.insertUserDietFoodMap(map);
        }

        log.info("[DietService] {} 식사 저장 완료", meal.getMealType());
    }

    @Override
    @Transactional
    public List<Diet> getDietsByTakeAt(Long userId, LocalDate takeAt) {
        return dietRepository.findDietsByUserIdAndTakeAt(userId, takeAt);
    }

    @Override
    @Transactional
    public List<Diet> getDietsByUserId(Long userId) {
        return dietRepository.findDietsByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteDietByUserIdAndDietId(Long userId,Long dietId) {
        Diet diet = dietRepository.findById(dietId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!diet.getUserId().equals(userId)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }
        dietRepository.deleteById(dietId);
        log.info("[DietService] Diet 삭제 성공: {}", dietId);
    }

    @Override
    @Transactional
    public void updateMealType(Long userId, Long dietId, MealType mealType) {
        Diet diet = dietRepository.findById(dietId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!diet.getUserId().equals(userId)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }

        diet.setMealType(mealType);
        dietRepository.save(diet);
        log.info("[DietService] Diet MealType {}으로 수정 성공", mealType.name());
    }

    @Override
    @Transactional
    public void updateTakeAt(Long userId, Long dietId, LocalDate takeAt) {
        Diet diet = dietRepository.findById(dietId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        diet.setTakeAt(takeAt);
        dietRepository.save(diet);
        log.info("[DietService] Diet takeAt {}으로 수정 성공", takeAt.toString());
    }

}
