package com.hankki.domain.diet.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietGetByDateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;
import com.hankki.domain.diet.dto.MealItemPreviewResponseDto;
import com.hankki.domain.diet.entity.Diet;
import com.hankki.domain.diet.entity.DietMealItem;
import com.hankki.domain.diet.entity.MealItem;
import com.hankki.domain.diet.mapper.DietMapper;
import com.hankki.domain.diet.mapper.DietMealItemMapper;
import com.hankki.domain.diet.mapper.MealItemMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DietServiceImpl implements DietService {

    private final DietMapper dietMapper;
    private final DietMealItemMapper dietMealItemMapper;
    private final MealItemMapper mealItemMapper;

    @Transactional
    @Override
    public void createDiet(DietCreateRequestDto requestDto) {
        log.info("[DietService] Diet 생성 Request : {}", requestDto);

        /*
        TO-DO: 중복, 유효 검사
         */
        try {
            Diet createdDiet = requestDto.toEntity();
            dietMapper.insertDiet(createdDiet);

            for (Long itemId : requestDto.getMealItemIds()) {
                DietMealItem relationalItem = DietMealItem.builder()
                        .dietId(createdDiet.getId())
                        .mealItemId(itemId)
                        .build();
                dietMealItemMapper.insertDietMealItem(relationalItem);
            }
            log.info("[DietService] Diet 및 관련 MealItem 저장 성공");
        } catch (Exception e) {
            log.error("[ERROR] DietService: Diet 엔티티 생성 실패: {}", e.getMessage(), e);
            throw new HankkiWikiException(ExceptionStatus.FAIL_TO_CREATE_ENTITY);
        }
    }

    @Override
    public DietResponseDto getMealItemByTakeAtAndMealType(DietGetByDateRequestDto requestDto) {
        log.info("[DietService] 회원의 해당 일자별 Diet 조회 Request : {}", requestDto);
        try {
            Diet diet = dietMapper.getDietByDate(
                    requestDto.getEmail(),
                    requestDto.getTakeAt(),
                    requestDto.getMealType());
            if (diet == null) {
                throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET);
            }

            List<Long> mealItemIds = dietMealItemMapper.findMealItemIdsByDietId(diet.getId());

            List<MealItem> foundMealItem = mealItemMapper.findPreviewByIds(mealItemIds);

            List<MealItemPreviewResponseDto> mealItems = foundMealItem.stream()
                    .map(item -> MealItemPreviewResponseDto.builder()
                            .id(item.getId())
                            .foodName(item.getFoodName())
                            .majorCategory(item.getMajorCategory())
                            .build())
                    .toList();

            return DietResponseDto.builder()
                    .id(diet.getId())
                    .email(diet.getEmail())
                    .mealType(diet.getMealType())
                    .dietMemo(diet.getDietMemo())
                    .mealItems(mealItems)
                    .build();

        } catch (Exception e) {
            log.error("[ERROR] DietService: MealItem 조회 실패: {}", e.getMessage(), e);
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_MEAL_ITEM);
        }
    }
}
