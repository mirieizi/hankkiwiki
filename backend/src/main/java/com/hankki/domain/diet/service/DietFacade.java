package com.hankki.domain.diet.service;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;
import com.hankki.domain.diet.dto.DietUpdateMealTypeRequestDto;
import com.hankki.domain.diet.dto.GroupedDietResponseDto;
import com.hankki.domain.food.dto.FoodGroupDto;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import com.hankki.domain.diet.entity.Diet;
import com.hankki.domain.diet.mapper.DietMealItemMapper;
import com.hankki.domain.food.service.FoodQueryServiceImpl;
import com.hankki.domain.user.repository.UserRepository;
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

    public List<DietResponseDto> getDietsByUserId(Long userId) {
        String email = userRepository.findById(userId).orElseThrow().getEmail();
        List<Diet> dietList = dietService.getDietsByEmail(email);
        return dietList.stream()
                .map(diet -> DietResponseDto.builder()
                        .id(diet.getId())
                        .takeAt(diet.getTakeAt())
                        .mealType(diet.getMealType())
                        .build())
                .toList();
    }

    /**
     * 현재 유저 email과 dietId로 찾은 userEmail 같은지 비교
     * 검증 후, dietId를 Diet, DietMealItem DB에서 삭제
     * @param email
     * @param dietId
     */
    public void deleteDietById(String email, Long dietId) {
        dietService.deleteDietById(email, dietId);
        dietMealItemMapper.deleteByDietId(dietId);
    }

    /**
     * diet의 식사 타입 변경
     * @param email
     * @param requestDto
     */
    public void updateMealType(String email, DietUpdateMealTypeRequestDto requestDto) {
        dietService.updateMealType(email, requestDto.getDietId(), requestDto.getMealType());
    }
}
