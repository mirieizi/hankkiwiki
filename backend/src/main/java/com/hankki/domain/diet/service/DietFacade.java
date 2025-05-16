package com.hankki.domain.diet.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.diet.dto.*;
import com.hankki.domain.food.dto.FoodGroupDto;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import com.hankki.domain.diet.entity.Diet;
import com.hankki.domain.diet.mapper.DietMealItemMapper;
import com.hankki.domain.food.service.FoodQueryServiceImpl;
import com.hankki.domain.user.repository.UserRepository;
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
    private final DietMealItemMapper dietMealItemMapper;
    private final FoodQueryServiceImpl foodQueryService;
    private final UserRepository userRepository;

    /**
     * Diet 생성
     * @param email UserDetails에서 온 현재 사용자 이메일
     * @param requestDto 생성하고자 하는 Diet 요청 Dto
     */
    public void createDiet(String email, DietCreateRequestDto requestDto) {
        dietService.createDiet(email, requestDto);
    }

    /**
     * 사용자의 해당 일자의 Diet를 모두 조회, mealType에 따라 나뉜 dietId를 해당 일자로 검색 -> 중간 테이블에서 MealItem을 검색
     * -> MealItem의 일부 정보만 ResponseDto로 감싼다.
     * 이때 mealType별로 food가 묶인다.
     * @param email
     * @param takeAt
     * @return GroupedDietResponseDto
     */
    public GroupedDietResponseDto getDietsByDate(String email, LocalDate takeAt) {
        List<Diet> dietList = dietService.getDietsByTakeAt(email, takeAt);

        List<FoodGroupDto> foods = dietList.stream()
                .map(diet -> {
                    List<Long> foodIds = dietMealItemMapper.findFoodIdIdsByDietId(diet.getId());
                    List<FoodPreviewResponseDto> foodPreviews = foodQueryService.getFoodPreviews(foodIds);
                    return FoodGroupDto.builder()
                            .mealType(diet.getMealType())
                            .foods(foodPreviews)
                            .build();
                })
                .toList();

        return GroupedDietResponseDto.builder()
                .takeAt(takeAt)
                .foods(foods)
                .build();
    }

    /**
     * diet의 식사 타입 변경
     * @param email
     * @param requestDto
     */
    public void updateMealType(String email, DietUpdateMealTypeRequestDto requestDto) {
        dietService.updateMealType(email, requestDto.getDietId(), requestDto.getMealType());
    }

    /*********************************
     *      admin 기능 관리 구역        *
     *********************************/

    public List<DietResponseDto> getDietsByUserId(Long userId) {
        try {
            String email = userRepository.findById(userId).orElseThrow().getEmail();
            List<Diet> dietList = dietService.getDietsByEmail(email);
            return dietList.stream()
                    .map(diet -> DietResponseDto.builder()
                            .id(diet.getId())
                            .takeAt(diet.getTakeAt())
                            .mealType(diet.getMealType())
                            .build())
                    .toList();
        } catch (Exception e) {
            log.error("[DietFacade] 사용자 조회 실패 - userId: {}", userId);
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER);
        }
    }

    public void updateDietInfo(Long dietId, DietUpdateRequestDto requestDto) {
        if (!dietId.equals(requestDto.getDietId())) {
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET);
        }

        if (requestDto.getMealType() != null) {
            dietService.updateMealType(requestDto.getEmail(), requestDto.getDietId(), requestDto.getMealType());
        }

        if (requestDto.getTakeAt() != null) {
            dietService.updateTakeAt(requestDto.getEmail(), requestDto.getDietId(), requestDto.getTakeAt());
        }
    }

    public void deleteDietByIdByAdmin(Long dietId) {
        dietService.deleteDietByDietId(dietId);
    }
}
