package com.hankki.domain.food.service;

import com.hankki.domain.diet.service.DietServiceImpl;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.recommend.dto.FoodResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodQueryServiceImpl {

    private final DietServiceImpl dietService;
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
        List<Long> foodIds = dietService.findRecentFoodIdsByUserId(userId, threeDaysAgo, today);
        if (foodIds.isEmpty()) throw new IllegalStateException("최근 섭취한 음식이 없습니다.");
        return foodIds;
    }

    /**
     * 음식 검색 (정확 매칭 우선, 풀텍스트 검색 보조)
     * @param query 검색어
     * @return 검색된 음식 목록 (최대 10개)
     */
    public List<FoodResponseDto> searchFoods(String query) {
        List<FoodResponseDto> results = new ArrayList<>();

        // 1. 정확한 이름 매칭 (대소문자 무시)
        Optional<Food> exactMatch = foodRepository.findByFoodNameIgnoreCase(query);
        if (exactMatch.isPresent()) {
            results.add(FoodResponseDto.from(exactMatch.get()));
            return results; // 정확한 매칭이 있으면 바로 반환
        }

        // 2. 풀텍스트 검색
        try {
            Optional<Food> fullTextMatch = foodRepository.findBestMatchByFullText(query);
            if (fullTextMatch.isPresent()) {
                results.add(FoodResponseDto.from(fullTextMatch.get()));
            }
        } catch (Exception e) {
            // 풀텍스트 인덱스가 설정되지 않은 경우 무시
            log.warn("풀텍스트 검색 실패: {}", e.getMessage());
        }

        // 3. 부분 매칭 검색 (LIKE 검색)
        if (results.isEmpty()) {
            List<Food> partialMatches = foodRepository.findByFoodNameContainingIgnoreCase(query);
            results.addAll(partialMatches.stream()
                    .limit(10)
                    .map(FoodResponseDto::from)
                    .toList());
        }

        return results;
    }
}
