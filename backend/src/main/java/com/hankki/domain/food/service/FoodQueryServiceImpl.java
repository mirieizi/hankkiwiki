package com.hankki.domain.food.service;

import com.hankki.domain.recommend.mapper.UserDietFoodMapper;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import com.hankki.domain.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodQueryServiceImpl {

    private final UserDietFoodMapper userDietFoodMapper;
    private final FoodRepository foodRepository;

    /**
     * Food의 Id에 해당하는 Food 조회 후 FoodPreview의 리스트로 반환
     * foodId, foodName, majorCategory, kcal만 담김
     * @param foodIds
     * @return FoodPreview들
     */
    public List<FoodPreviewResponseDto> getFoodPreviews(List<Long> foodIds) {
        if (foodIds == null || foodIds.isEmpty()) {
            return List.of(); // 빈 리스트 안전 처리
        }

        return foodRepository.findPreviewsByIds(foodIds);
    }

    public List<Long> findFoodsByUserIdAndTakeAtBetween(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(2);
        List<Long> foodIds = userDietFoodMapper.findFoodIdsByUserIdAndTakeAtBetween(userId, threeDaysAgo, today);
        if (foodIds.isEmpty()) throw new IllegalStateException("최근 섭취한 음식이 없습니다.");
        return foodIds;
    }

}
