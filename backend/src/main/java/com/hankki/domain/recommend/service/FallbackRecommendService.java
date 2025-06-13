package com.hankki.domain.recommend.service;

import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

// 새로운 폴백 전용 서비스 생성
@Service
@RequiredArgsConstructor
@Slf4j
public class FallbackRecommendService {

    private final FoodRepository foodRepository;

    /**
     * 벡터 추천 실패 시 사용할 폴백 음식 ID 반환
     */
    public Long getFallbackFoodId() {
        log.info("[FallbackRecommendService] 폴백 음식 ID 조회");

        try {
            // 성별에 맞는 랜덤 음식 조회
            Optional<Food> randomFood = foodRepository.findRandomFood();

            if (randomFood.isPresent()) {
                return randomFood.get().getId();
            }

            // 성별 상관없이 랜덤 음식 조회
            return foodRepository.findRandomFood()
                    .map(Food::getId)
                    .orElse(getDefaultFoodId());

        } catch (Exception e) {
            log.error("[FallbackRecommendService] 폴백 음식 조회 실패", e);
            return getDefaultFoodId();
        }
    }

    private Long getDefaultFoodId() {
        return foodRepository.findFirstByOrderById()
                .map(Food::getId)
                .orElse(1L);
    }
}
