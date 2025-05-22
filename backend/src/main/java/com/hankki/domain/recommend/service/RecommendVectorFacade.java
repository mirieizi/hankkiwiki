package com.hankki.domain.recommend.service;

import com.hankki.common.redis.vector.RedisVectorSearcher;
import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.recommend.repository.UserFoodLogRepository;
import com.hankki.domain.user.constant.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendVectorFacade {

    private final UserFoodLogRepository userFoodLogRepository;
    private final RedisVectorSearcher redisVectorSearcher;
    private final FoodRepository foodRepository;

    public Long findNeutralFoodFromRecent(Long userId, Gender gender) {
        List<Long> recentFoodIds = loadRecentFoodIds(userId);
        double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
        List<Long> sortedIds = redisVectorSearcher.knnSearch(gender, avgVector, 11);
        return sortedIds.get(sortedIds.size() / 2); // 중간값
    }

    public Long findMostSimilarFoodFromRecent(Long userId, Gender gender) {
        List<Long> recentFoodIds = loadRecentFoodIds(userId);
        double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
        return redisVectorSearcher.knnSearch(gender, avgVector, 1).get(0);
    }

    public Long findFurthestFoodFromRecent(Long userId, Gender gender) {
        List<Long> recentFoodIds = loadRecentFoodIds(userId);
        double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
        return redisVectorSearcher.furthestSearch(gender, avgVector);
    }

    private List<Long> loadRecentFoodIds(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(2);
        List<Long> foodIds = userFoodLogRepository.findAllFoodIdsByUserIdAndTakeAtBetween(userId, threeDaysAgo, today);
        if (foodIds.isEmpty()) throw new IllegalStateException("최근 섭취한 음식이 없습니다.");
        return foodIds;
    }
    
    public List<Food> getRecentFoods(Long userId){
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(2);
    	List<Long> recentFoodIds = userFoodLogRepository.findAllFoodIdsByUserIdAndTakeAtBetween(userId, threeDaysAgo, today);
        if (recentFoodIds.isEmpty()) {
            throw new IllegalStateException("최근 섭취한 음식이 없습니다.");
        }
        // Food 엔티티 리스트로 변환 반환
        return foodRepository.findAllById(recentFoodIds);
    }

}
