package com.hankki.domain.diet.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.dto.*;
import com.hankki.domain.diet.entity.Diet;
import com.hankki.domain.diet.entity.DietMealItem;
import com.hankki.domain.diet.mapper.DietMealItemMapper;
import com.hankki.domain.diet.repository.DietRepository;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
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

    private DietRepository dietRepository;
    private DietMealItemMapper dietMealItemMapper;

    @Override
    @Transactional
    public void createDiet(String email, DietCreateRequestDto requestDto) {
        log.info("[DietService] Diet 생성 Request : {}", requestDto);
        // Diet 요청 값 저장
        Diet createdDiet = requestDto.toEntity(email);

        // Diet에 대한 MealItem(food)의 값 중간 테이블에 저장
        for (Long itemId : requestDto.getMealItemIds()) {
            DietMealItem dietMealItem = DietMealItem.builder()
                    .dietId(createdDiet.getId())
                    .mealItemId(itemId)
                    .build();
            dietMealItemMapper.insertDietMealItem(dietMealItem);
        }
        log.info("[DietService] Diet 생성 완료");
    }

    @Override
    @Transactional
    public List<Diet> getDietsByTakeAt(String email, LocalDate takeAt) {
        return dietRepository.findDietsByEmailAndTakeAt(email, takeAt);
    }

    @Override
    @Transactional
    public List<Diet> getDietsByEmail(String email) {
        return dietRepository.findDietsByEmail(email);
    }

    @Override
    @Transactional
    public void deleteDietById(String email,Long dietId) {
        Diet diet = dietRepository.findById(dietId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!diet.getEmail().equals(email)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }
        dietRepository.deleteById(dietId);
        log.info("[DietService] Diet 삭제 성공");
    }

    @Override
    @Transactional
    public void updateMealType(String email, Long dietId, MealType mealType) {
        Diet diet = dietRepository.findById(dietId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!diet.getEmail().equals(email)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }

        diet.setMealType(mealType);
        log.info("[DietService] Diet MealType {}으로 수정 성공", mealType.name());
    }

    @Override
    @Transactional
    public void updateTakeAt(String email, Long dietId, LocalDate takeAt) {
        Diet diet = dietRepository.findById(dietId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        diet.setTakeAt(takeAt);
        log.info("[DietService] Diet takeAt {}으로 수정 성공", takeAt.toString());
    }

}
