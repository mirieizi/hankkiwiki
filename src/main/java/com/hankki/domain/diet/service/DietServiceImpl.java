package com.hankki.domain.diet.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
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

    @Transactional
    @Override
    public void createDiet(String email, DietCreateRequestDto requestDto) {
        log.info("[DietService] Diet 생성 Request : {}", requestDto);
        // Diet 요청 값 저장
        Diet createdDiet = requestDto.toEntity(email);
        dietRepository.insertDiet(createdDiet);

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
    public List<Diet> getDietsByTakeAt(String email, LocalDate takeAt) {
        return dietRepository.findDietsByEmailAndTakeAt(email, takeAt);
    }

    @Override
    public void deleteDietById(String email,Long dietId) {
        Diet diet = dietRepository.findByDietId(dietId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!diet.getEmail().equals(email)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }
    }

    @Override
    public void updateDietInfo(DietUpdateInfoRequestDto requestDto) {
        Diet diet = dietMapper.getDietById(requestDto.getDietId())
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));
        // 유저 검증 로직 추가 예정

        try {
            if (requestDto.getMealType() != null) {
                diet.setMealType(requestDto.getMealType());
            }

            if (requestDto.getDietMemo() != null) {
                diet.setDietMemo(requestDto.getDietMemo());
            }

            dietMapper.updateDietInfo(diet);
            log.info("[DietService] Diet 정보 수정 성공");
        } catch (Exception e) {
            log.error("[ERROR] Diet 정보 수정 실패: {}", e.getMessage(), e);
            throw new HankkiWikiException(ExceptionStatus.FAIL_TO_UPDATE_ENTITY);
        }
    }

    private DietResponseDto toDietResponseDto(Diet diet) {
        // 중간 테이블에서 mealItemId 조회
        List<Long> mealItemIds = dietMealItemMapper.findFoodIdIdsByDietId(diet.getId());

        // MealItem 엔티티 조회
        List<FoodPreviewResponseDto> mealItems = mealItemMapper.findPreviewByIds(mealItemIds)
                .stream()
                .map(item -> FoodPreviewResponseDto.builder()
                        .id(item.getId())
                        .foodName(item.getFoodName())
                        .majorCategory(item.getMajorCategory())
                        .build())
                .toList();

        return DietResponseDto.builder()
                .id(diet.getId())
                .email(diet.getEmail())
                .takeAt(diet.getTakeAt())
                .mealType(diet.getMealType())
                .dietMemo(diet.getDietMemo())
                .mealItems(mealItems)
                .build();
    }

    private void validateUserByEmail(String email, String dtoEmail) {
        if (email.isBlank() || dtoEmail.isBlank() || !email.equals(dtoEmail)) {
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER);
        }

        dietMapper.getDietByEmail(email);
    }


}
