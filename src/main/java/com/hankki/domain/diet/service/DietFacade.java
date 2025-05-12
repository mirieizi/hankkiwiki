package com.hankki.domain.diet.service;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;
import com.hankki.domain.diet.dto.GroupedDietResponseDto;
import com.hankki.domain.food.dto.FoodGroupDto;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import com.hankki.domain.diet.entity.Diet;
import com.hankki.domain.diet.mapper.DietMealItemMapper;
import com.hankki.domain.food.service.FoodQueryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietFacade {

    private final DietService dietService;
    private final DietMealItemMapper dietMealItemMapper;
    private final FoodQueryServiceImpl foodQueryService;

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

    public void deleteDietById(String email, Long dietId) {
        dietService.deleteDietById(email, dietId);
        dietMealItemMapper.deleteByDietId(dietId);
    }

}
