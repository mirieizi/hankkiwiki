package com.hankki.domain.recommend.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.diet.repository.DietFoodRepository;
import com.hankki.domain.diet.service.DietService;
import com.hankki.domain.diet.service.DietServiceImpl;
import com.hankki.domain.recommend.repository.UserFoodLogRepository;
import com.hankki.domain.vector.util.RedisVectorSearcher;
import com.hankki.domain.user.constant.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendVectorFacade {

    private final UserFoodLogRepository userFoodLogRepository;
    private final DietServiceImpl dietService;
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

        try {
            double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
            List<Long> results = redisVectorSearcher.furthestSearch(gender, avgVector, 30);
            return checkDuplicatedRecommend(results, recentFoodIds, recentRecommendedFoodIds);
        } catch (Exception e) {
            log.error("[RecommendVectorFacade] 가장 먼 음식 찾기 실패: {}", e.getMessage(), e);
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
        }

    }

    public Long findMostSimilarFoodFromRecent(Long userId, Gender gender) {
        List<Long> recentFoodIds = loadRecentUserFoodIds(userId);
        List<Long> recentRecommendedFoodIds = loadRecentRecommendedFoodIds(userId);

        try {
            double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
            List<Long> results = redisVectorSearcher.knnSearch(gender, avgVector, 1);
            return checkDuplicatedRecommend(results, recentFoodIds, recentRecommendedFoodIds);
        } catch (Exception e) {
            log.error("[RecommendVectorFacade] 가장 가까운 음식 찾기 실패: {}", e.getMessage(), e);
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
        }
    }

    private List<Long> loadRecentUserFoodIds(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(2);
        return dietService.findRecentFoodIdsByUserId(userId, threeDaysAgo, today);
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
        if (foodIds == null || foodIds.isEmpty()) {
            log.warn("[checkDuplicatedRecommend] 추천 후보군이 비어 있습니다. (벡터 검색 실패 또는 Redis 문제)");
            throw new IllegalStateException("추천할 음식이 없습니다.");
        }

        Set<Long> recent = new HashSet<>();
        recent.addAll(recentTakenFoodIds);
        recent.addAll(recentRecommendedFoodIds);

        return foodIds.stream()
                .filter(id -> !recent.contains(id))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("[checkDuplicatedRecommend] 모든 후보가 최근 섭취 또는 추천 목록에 포함됨");
                    return new IllegalStateException("중복을 제외한 추천 후보가 없습니다.");
                });

    }
}
