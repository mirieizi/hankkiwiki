package com.hankki.domain.diet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.stereotype.Service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.GroupedDietResponseDto;
import com.hankki.domain.diet.entity.DietFood;
import com.hankki.domain.diet.entity.DietGroup;
import com.hankki.domain.diet.repository.DietFoodRepository;
import com.hankki.domain.diet.repository.DietGroupRepository;
import com.hankki.domain.food.dto.FoodGroupDto;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DietServiceImpl implements DietService {

    private final DietGroupRepository dietGroupRepository;
    private final DietFoodRepository dietFoodRepository;
    private final FoodRepository foodRepository;

    @Override
    @Transactional
    public void createDiet(Long userId, DietCreateRequestDto requestDto) {
        log.info("[DietService] 식단 생성 시작: userId={}, takeAt={}, meals={}",
                userId, requestDto.getTakeAt(), requestDto.getFoods().size());

        int successCount = 0;
        int failCount = 0;

        for (DietCreateRequestDto.MealWithFoods meal : requestDto.getFoods()) {
            try {
                dietOneMeal(userId, requestDto.getTakeAt(), meal);
                successCount++;
                log.info("[DietService] {} 식사 생성 성공", meal.getMealType());
            } catch (Exception e) {
                failCount++;
                log.error("[DietService] {} 식사 생성 중 오류 발생: {}", meal.getMealType(), e.getMessage(), e);
                throw new HankkiWikiException(ExceptionStatus.INTERNAL_SERVER_ERROR);
            }
        }

        log.info("[DietService] 식단 생성 완료: 성공={}, 실패={}", successCount, failCount);
    }

    @Transactional
    public void dietOneMeal(Long userId, LocalDate takeAt, DietCreateRequestDto.MealWithFoods meal) {
        log.debug("[DietService] {} 식사 처리 시작: userId={}, takeAt={}, foodIds={}",
                meal.getMealType(), userId, takeAt, meal.getFoodIds().size());

        DietGroup existingDietGroup = dietGroupRepository.findByUserIdAndTakeAtAndMealType(userId, takeAt, meal.getMealType());

        if (existingDietGroup != null) {
            log.info("[DietService] 기존 식단 그룹 발견: id={}", existingDietGroup.getId());
            addFoodsToExistingGroup(existingDietGroup, meal.getFoodIds());
            return;
        }

        DietGroup newDietGroup = createNewDietGroup(userId, takeAt, meal);
        addFoodsToNewGroup(newDietGroup, meal.getFoodIds());
    }

    private void addFoodsToExistingGroup(DietGroup dietGroup, List<Long> foodIds) {
        int addedCount = 0;

        for (Long foodId : foodIds) {
            if (!dietFoodRepository.existsByDietGroupIdAndFoodId(dietGroup.getId(), foodId)) {
                DietFood dietFood = DietFood.builder()
                        .dietGroupId(dietGroup.getId())
                        .foodId(foodId)
                        .createdAt(LocalDateTime.now())
                        .build();

                DietFood savedDietFood = dietFoodRepository.save(dietFood);
                log.debug("[DietService] 기존 그룹에 음식 추가: dietFoodId={}, dietGroupId={}, foodId={}",
                        savedDietFood.getId(), dietGroup.getId(), foodId);
                addedCount++;
            } else {
                log.debug("[DietService] 이미 존재하는 음식 스킵: dietGroupId={}, foodId={}",
                        dietGroup.getId(), foodId);
            }
        }

        log.info("[DietService] 기존 식단 그룹에 음식 추가 완료: 추가된 음식 수={}", addedCount);
    }

    private DietGroup createNewDietGroup(Long userId, LocalDate takeAt, DietCreateRequestDto.MealWithFoods meal) {
        DietGroup dietGroup = DietGroup.builder()
                .userId(userId)
                .takeAt(takeAt)
                .mealType(meal.getMealType())
                .createdAt(LocalDateTime.now())
                .build();

        DietGroup savedDietGroup = dietGroupRepository.save(dietGroup);

        if (savedDietGroup.getId() == null) {
            log.error("[DietService] 식단 그룹 저장 실패: ID가 생성되지 않음");
            throw new HankkiWikiException(ExceptionStatus.INTERNAL_SERVER_ERROR);
        }

        log.info("[DietService] 새 식단 그룹 생성 완료: id={}, userId={}, takeAt={}, mealType={}",
                savedDietGroup.getId(), userId, takeAt, meal.getMealType());

        return savedDietGroup;
    }

    private void addFoodsToNewGroup(DietGroup dietGroup, List<Long> foodIds) {
        log.debug("[DietService] 새 식단 그룹에 음식 추가 시작: dietGroupId={}, foodIds={}",
                dietGroup.getId(), foodIds.size());

        for (Long foodId : foodIds) {
            DietFood dietFood = DietFood.builder()
                    .dietGroupId(dietGroup.getId())
                    .foodId(foodId)
                    .createdAt(LocalDateTime.now())
                    .build();

            log.debug("[DietService] 음식 저장 전: dietGroupId={}, foodId={}",
                    dietGroup.getId(), foodId);

            DietFood savedDietFood = dietFoodRepository.save(dietFood);

            if (savedDietFood.getId() == null) {
                log.error("[DietService] 음식 저장 실패: dietGroupId={}, foodId={}",
                        dietGroup.getId(), foodId);
                throw new HankkiWikiException(ExceptionStatus.INTERNAL_SERVER_ERROR);
            }

            log.debug("[DietService] 음식 저장 완료: dietFoodId={}, dietGroupId={}, foodId={}",
                    savedDietFood.getId(), dietGroup.getId(), foodId);
        }

        log.info("[DietService] 새 식단 그룹에 음식 추가 완료: dietGroupId={}, 추가된 음식 수={}",
                dietGroup.getId(), foodIds.size());
    }

    @Override
    @Transactional
    public List<DietGroup> getDietsByTakeAt(Long userId, LocalDate takeAt) {
        return dietGroupRepository.findDietsByUserIdAndTakeAt(userId, takeAt);
    }



    @Override
    @Transactional
    public List<DietGroup> getDietsByUserId(Long userId) {
        return dietGroupRepository.findDietsByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteDietByUserIdAndDietId(Long userId, Long dietGroupId) {
        DietGroup dietGroup = dietGroupRepository.findById(dietGroupId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!dietGroup.getUserId().equals(userId)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }

        dietFoodRepository.deleteByDietGroupId(dietGroupId);
        dietGroupRepository.deleteById(dietGroupId);
        log.info("[DietService] DietGroup 삭제 성공: {}", dietGroupId);
    }

    @Override
    @Transactional
    public void updateMealType(Long userId, Long dietGroupId, MealType mealType) {
        DietGroup dietGroup = dietGroupRepository.findById(dietGroupId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!dietGroup.getUserId().equals(userId)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }

        dietGroup.setMealType(mealType);
        DietGroup savedDietGroup = dietGroupRepository.save(dietGroup);  // 반환값 사용
        log.info("[DietService] DietGroup MealType {}(으)로 수정 성공: id={}",
                mealType.name(), savedDietGroup.getId());
    }

    @Override
    @Transactional
    public void updateTakeAt(Long userId, Long dietGroupId, LocalDate takeAt) {
        DietGroup dietGroup = dietGroupRepository.findById(dietGroupId)
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_DIET));

        if (!dietGroup.getUserId().equals(userId)) {
            throw new HankkiWikiException(ExceptionStatus.ACCESS_DENIED);
        }

        dietGroup.setTakeAt(takeAt);
        DietGroup savedDietGroup = dietGroupRepository.save(dietGroup);  // 반환값 사용
        log.info("[DietService] DietGroup takeAt {}(으)로 수정 성공: id={}",
                takeAt, savedDietGroup.getId());
    }

    @Override
    public List<Long> findRecentFoodIdsByUserId(Long userId, LocalDate start, LocalDate end) {
        List<Long> groupIds = dietGroupRepository.findIdsByUserIdAndTakeAtBetween(userId, start, end);
        if (groupIds.isEmpty()) return List.of();

        return dietFoodRepository.findFoodIdsByDietGroupIdIn(groupIds);
    }

	@Override
	public boolean hasDietHistoryForRecentDays(Long userId, int days) {
	    LocalDate from = LocalDate.now().minusDays(days - 1); // 오늘 포함 N일 전
	    LocalDate to = LocalDate.now();
	    return dietGroupRepository.existsByUserIdAndTakeAtBetween(userId, from, to);
	}


	@Override
	public List<GroupedDietResponseDto> getGroupedDietsByRecentDays(Long userId, int days) {
	    LocalDate from = LocalDate.now().minusDays(days - 1); // 오늘 포함 N일 전
	    LocalDate to = LocalDate.now();

	    List<DietGroup> groups = dietGroupRepository.findDietsByUserIdAndTakeAtBetween(userId, from, to);

	    List<GroupedDietResponseDto> result = new ArrayList<>();

	    for (DietGroup group : groups) {
	        List<Long> foodIds = dietFoodRepository.findFoodIdsByDietGroupId(group.getId());
	        List<Food> foods = foodRepository.findAllById(foodIds);

	        // 1. Food -> FoodPreviewResponseDto 리스트 생성
	        List<FoodPreviewResponseDto> foodPreviews = foods.stream()
	            .map(food -> food.toPreviewDto())  // Food 엔티티에 toPreviewDto() 메서드 필요
	            .collect(Collectors.toList());

	        // 2. FoodGroupDto 객체 생성
	        FoodGroupDto foodGroupDto = FoodGroupDto.builder()
	            .dietId(group.getId())
	            .mealType(group.getMealType())
	            .foods(foodPreviews)
	            .build();

	        // 3. FoodGroupDto 리스트로 만들어서 넣기
	        List<FoodGroupDto> foodGroupDtos = List.of(foodGroupDto);

	        GroupedDietResponseDto dto = GroupedDietResponseDto.builder()
	            .takeAt(group.getTakeAt())
	            .foods(foodGroupDtos)  // 여기에 List<FoodGroupDto> 넣어야 함
	            .build();

	        result.add(dto);
	    }
	    return result;
	}

	@Override
	public List<Food> findRecentFoods(Long userId, int days) {
	    LocalDate from = LocalDate.now().minusDays(days - 1);
	    LocalDate to = LocalDate.now();
	    List<Long> groupIds = dietGroupRepository.findIdsByUserIdAndTakeAtBetween(userId, from, to);
	    if (groupIds.isEmpty()) return List.of();

	    List<Long> foodIds = dietFoodRepository.findFoodIdsByDietGroupIdIn(groupIds);
	    if (foodIds.isEmpty()) return List.of();

	    return foodRepository.findAllById(foodIds);
	}








}
