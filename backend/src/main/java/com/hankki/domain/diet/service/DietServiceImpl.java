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
    @Transactional
    public void createDiet(Long userId, DietCreateRequestDto requestDto) {
        log.info("[DietService] Diet 생성 Request : {}", requestDto);
        // Diet 요청 값 저장
        Diet createdDiet = requestDto.toEntity(userId);

        // Diet에 대한 MealItem(food)의 값 중간 테이블에 저장
        for (Long itemId : requestDto.getMealItemIds()) {
            UserDietFoodMap userDietFoodMap = UserDietFoodMap.builder()
                    .dietId(createdDiet.getId())
                    .foodId(itemId)
                    .build();
            userDietFoodMapper.insertUserDietFoodMap(userDietFoodMap);
        }
        dietRepository.save(createdDiet);
        log.info("[DietService] Diet 생성 완료");
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
