package com.hankki.domain.diet.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.diet.dto.*;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import com.hankki.domain.diet.entity.DietGroup;
import com.hankki.domain.diet.repository.DietFoodRepository;
import com.hankki.domain.food.service.FoodQueryServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DietFacade {

    private final DietService dietService;
    private final DietFoodRepository dietFoodRepository;
    private final FoodQueryServiceImpl foodQueryService;

    /**
     * Diet 생성
     * @param userId UserDetails에서 온 현재 사용자 이메일
     * @param requestDto 생성하고자 하는 Diet 요청 Dto
     */
    public void createDiet(Long userId, DietCreateRequestDto requestDto) {
        dietService.createDiet(userId, requestDto);
    }

    /**
     * 사용자의 해당 일자의 Diet를 모두 조회, mealType에 따라 나뉜 dietId를 해당 일자로 검색 -> 중간 테이블에서 MealItem을 검색
     * -> MealItem의 일부 정보만 ResponseDto로 감싼다.
     * 이때 mealType별로 food가 묶인다.
     * @param userId
     * @param takeAt
     * @return GroupedDietResponseDto
     */
    public List<DietResponseDto> getDietsByDate(Long userId, LocalDate takeAt) {
        List<DietGroup> dietGroups = dietService.getDietsByTakeAt(userId, takeAt);

        return dietGroups.stream()
                .map(group -> {
                    List<Long> foodIds = dietFoodRepository.findFoodIdsByDietGroupId(group.getId());
                    List<FoodPreviewResponseDto> foodPreviews = foodQueryService.getFoodPreviews(foodIds);

                    return DietResponseDto.builder()
                            .id(group.getId())
                            .takeAt(group.getTakeAt())
                            .mealType(group.getMealType())
                            .foods(foodPreviews)
                            .build();
                })
                .toList();
    }

    /**
     * diet의 식사 타입 변경
     * @param userId
     * @param requestDto
     */
    public void updateMealType(Long userId, DietUpdateMealTypeRequestDto requestDto) {
        dietService.updateMealType(userId, requestDto.getDietId(), requestDto.getMealType());
    }

    public void deleteDietById(Long userId, Long dietId) {
        dietService.deleteDietByUserIdAndDietId(userId, dietId);
    }

    /*********************************
     *      admin 기능 관리 구역        *
     *********************************/

    public List<DietResponseDto> getDietsByUserId(Long userId) {
        try {
            List<DietGroup> dietGroupList = dietService.getDietsByUserId(userId);

            return dietGroupList.stream()
                    .map(dietGroup -> {
                        List<Long> foodIds = dietFoodRepository.findFoodIdsByDietGroupId(dietGroup.getId());
                        List<FoodPreviewResponseDto> foodPreviews = foodQueryService.getFoodPreviews(foodIds);

                        return DietResponseDto.builder()
                                .id(dietGroup.getId())
                                .takeAt(dietGroup.getTakeAt())
                                .mealType(dietGroup.getMealType())
                                .foods(foodPreviews)
                                .build();
                    })
                    .toList();
        } catch (Exception e) {
            log.error("[DietFacade] 사용자 조회 실패 - userId: {}", userId);
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER);
        }
    }


    public void updateDietInfo(Long userId, Long dietId, DietUpdateRequestDto requestDto) {
        if (!dietId.equals(requestDto.getDietId())) {
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET);
        }

        if (requestDto.getMealType() != null) {
            dietService.updateMealType(userId, requestDto.getDietId(), requestDto.getMealType());
        }

        if (requestDto.getTakeAt() != null) {
            dietService.updateTakeAt(userId, requestDto.getDietId(), requestDto.getTakeAt());
        }
    }

    public void deleteDietByIdByAdmin(Long userId, Long dietId) {
        dietService.deleteDietByUserIdAndDietId(userId, dietId);
    }

	public boolean hasDietHistoryForRecentDays(Long userId, int days) {
		return dietService.hasDietHistoryForRecentDays(userId, days);
	}

	public List<GroupedDietResponseDto> getGroupedDietsByRecentDays(Long userId, int days) {
	    return dietService.getGroupedDietsByRecentDays(userId, days);
	}
}
