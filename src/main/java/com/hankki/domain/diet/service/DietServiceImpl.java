package com.hankki.domain.diet.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;
import com.hankki.domain.diet.dto.DietUpdateInfoRequestDto;
import com.hankki.domain.diet.dto.MealItemPreviewResponseDto;
import com.hankki.domain.diet.entity.Diet;
import com.hankki.domain.diet.entity.DietMealItem;
import com.hankki.domain.diet.mapper.DietMapper;
import com.hankki.domain.diet.mapper.DietMealItemMapper;
import com.hankki.domain.diet.mapper.MealItemMapper;
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
    public List<DietResponseDto> getDietsByTakeAt(String email, LocalDate takeAt) {
        try {
            List<Diet> dietList = dietMapper.getDietByTakeAt(email, takeAt);

            return dietList.stream()
                    .map(this::toDietResponseDto)
                    .toList();

        } catch (Exception e) {
            log.error("[ERROR] DietService: MealItem 조회 실패: {}", e.getMessage(), e);
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_MEAL_ITEM);
        }
    }

    @Override
    public void deleteDietById(Long dietId) {
        try {
            Diet diet = dietMapper.getDietById(dietId)
                    .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));
            /*
            // 2) 권한(유저 일치) 확인
            if (!diet.getEmail().equals(currentUserEmail)) {
                throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
            }
             */

            dietMealItemMapper.deleteByDietId(dietId);
            dietMapper.deleteDietById(dietId);

        } catch (Exception e) {
            log.error("[ERROR] DietService: Diet 삭제 실패: {}", e.getMessage(), e);
            throw new HankkiWikiException(ExceptionStatus.FAIL_TO_DELETE_ENTITY);
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
        List<Long> mealItemIds = dietMealItemMapper.findMealItemIdsByDietId(diet.getId());

        // MealItem 엔티티 조회
        List<MealItemPreviewResponseDto> mealItems = mealItemMapper.findPreviewByIds(mealItemIds)
                .stream()
                .map(item -> MealItemPreviewResponseDto.builder()
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


}
