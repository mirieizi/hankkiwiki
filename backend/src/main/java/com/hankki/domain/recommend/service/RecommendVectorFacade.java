package com.hankki.domain.recommend.service;

import com.hankki.domain.vector.util.RedisVectorSearcher;
import com.hankki.domain.food.service.FoodQueryServiceImpl;
import com.hankki.domain.user.constant.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendVectorFacade {

    private final FoodQueryServiceImpl foodQueryService;
    private final RedisVectorSearcher redisVectorSearcher;
    private final UserFoodLogServiceImpl userFoodLogService;

    /**
     * 최근 먹은 음식과 가장 거리가 먼 음식 찾기
     * 이때 UserDietFoodMap 중간 테이블을 통해 3일간 섭취한 음식을 기반으로 한다.
     * 벡터 거리 계산 Redis를 통해 실행된다
     * 이후 최근 먹은 음식, 추천 받은 음식 기록과의 중복을 제거한다.
     * @param userId
     * @param gender
     * @return
     */
    public Long findFurthestFoodFromRecent(Long userId, Gender gender) {
        List<Long> recentFoodIds = foodQueryService.findFoodsByUserIdAndTakeAtBetween(userId);
        double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
        List<Long> results = redisVectorSearcher.furthestSearch(gender, avgVector);
        return userFoodLogService.checkDuplicatedRecommend(userId, results);
    }

    public Long findNeutralFoodFromRecent(Long userId, Gender gender) {
        List<Long> recentFoodIds = foodQueryService.findFoodsByUserIdAndTakeAtBetween(userId);
        double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
        List<Long> sortedIds = redisVectorSearcher.knnSearch(gender, avgVector, 11);
        return sortedIds.get(sortedIds.size() / 2); // 중간값
    }

    public Long findMostSimilarFoodFromRecent(Long userId, Gender gender) {
        List<Long> recentFoodIds = foodQueryService.findFoodsByUserIdAndTakeAtBetween(userId);

        double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
        return redisVectorSearcher.knnSearch(gender, avgVector, 1).get(0);
    }

}
