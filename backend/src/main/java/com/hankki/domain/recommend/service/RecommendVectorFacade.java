package com.hankki.domain.recommend.service;

import com.hankki.domain.recommend.mapper.UserDietFoodMapper;
import com.hankki.domain.recommend.repository.UserFoodLogRepository;
import com.hankki.domain.vector.util.RedisVectorSearcher;
import com.hankki.domain.food.service.FoodQueryServiceImpl;
import com.hankki.domain.user.constant.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RecommendVectorFacade {

    private final UserFoodLogRepository userFoodLogRepository;
    private final UserDietFoodMapper userDietFoodMapper;
    private final RedisVectorSearcher redisVectorSearcher;

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
        List<Long> recentFoodIds = loadRecentUserFoodIds(userId);
        List<Long> recentRecommendedFoodIds = loadRecentRecommendedFoodIds(userId);
        double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
        List<Long> results = redisVectorSearcher.furthestSearch(gender, avgVector, 30);
        return checkDuplicatedRecommend(results, recentFoodIds, recentRecommendedFoodIds);
    }

    public Long findNeutralFoodFromRecent(Long userId, Gender gender) {
        List<Long> recentFoodIds = loadRecentUserFoodIds(userId);
        List<Long> recentRecommendedFoodIds = loadRecentRecommendedFoodIds(userId);
        double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
        List<Long> sortedIds = redisVectorSearcher.knnSearch(gender, avgVector, 21);

        return checkDuplicatedRecommend(sortedIds, recentFoodIds, recentRecommendedFoodIds);
    }

    public Long findMostSimilarFoodFromRecent(Long userId, Gender gender) {
        List<Long> recentFoodIds = loadRecentUserFoodIds(userId);
        List<Long> recentRecommendedFoodIds = loadRecentRecommendedFoodIds(userId);
        double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
        List<Long> results = redisVectorSearcher.knnSearch(gender, avgVector, 1);
        return checkDuplicatedRecommend(results, recentFoodIds, recentRecommendedFoodIds);
    }

    private List<Long> loadRecentUserFoodIds(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(2);
        return userDietFoodMapper.findFoodIdsByUserIdAndTakeAtBetween(userId, threeDaysAgo, today);
    }

    private List<Long> loadRecentRecommendedFoodIds(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(2);
        return userFoodLogRepository.findRecommendedFoodIdsByUserIdAndTakeAtBetween(userId, threeDaysAgo, today);
    }

    private Long checkDuplicatedRecommend(
            List<Long> foodIds,
            List<Long> recentTakenFoodIds,
            List<Long> recentRecommendedFoodIds
    ) {
        Set<Long> recent = new HashSet<>();
        recent.addAll(recentTakenFoodIds);
        recent.addAll(recentRecommendedFoodIds);

        return foodIds.stream()
                .filter(id -> !recent.contains(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("중복을 제외한 추천 후보가 없습니다."));

    }
}
