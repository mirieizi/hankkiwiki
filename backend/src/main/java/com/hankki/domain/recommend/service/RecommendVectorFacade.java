package com.hankki.domain.recommend.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.diet.repository.DietFoodRepository;
import com.hankki.domain.diet.repository.DietGroupRepository;
import com.hankki.domain.recommend.repository.UserFoodLogRepository;
import com.hankki.domain.vector.util.RedisVectorSearcher;
import com.hankki.domain.user.constant.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendVectorFacade {

    private static final int EXPECTED_VECTOR_DIMENSION = 9;
    private static final int MIN_RECENT_FOODS = 1; // 최소 최근 음식 수
    private static final int SEARCH_CANDIDATE_SIZE = 50; // 검색 후보 크기

    private final UserFoodLogRepository userFoodLogRepository;
    private final DietFoodRepository dietFoodRepository;
    private final DietGroupRepository dietGroupRepository;
    private final RedisVectorSearcher redisVectorSearcher;

    /**
     * 최근 먹은 음식과 가장 거리가 먼 음식 찾기
     */
    public Long findFurthestFoodFromRecent(Long userId, Gender gender) {
        log.info("[RecommendVectorFacade] 가장 먼 음식 찾기 시작: userId={}, gender={}", userId, gender);

        try {
            List<Long> recentFoodIds = loadRecentUserFoodIds(userId);
            List<Long> recentRecommendedFoodIds = loadRecentRecommendedFoodIds(userId);

            if (recentFoodIds.size() < MIN_RECENT_FOODS) {
                log.warn("[RecommendVectorFacade] 최근 음식 데이터 부족: userId={}, 최근 음식 수={}",
                        userId, recentFoodIds.size());
                throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
            }

            double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
            if (!isValidVector(avgVector)) {
                log.error("[RecommendVectorFacade] 평균 벡터 계산 실패: userId={}", userId);
                throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
            }

            List<Long> results = redisVectorSearcher.furthestSearch(gender, avgVector, SEARCH_CANDIDATE_SIZE);
            Long recommendedFoodId = checkDuplicatedRecommend(results, recentFoodIds, recentRecommendedFoodIds);

            log.info("[RecommendVectorFacade] 가장 먼 음식 찾기 완료: userId={}, foodId={}", userId, recommendedFoodId);
            return recommendedFoodId;

        } catch (HankkiWikiException e) {
            throw e;
        } catch (Exception e) {
            log.error("[RecommendVectorFacade] 가장 먼 음식 찾기 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
        }
    }

    public Long findMostSimilarFoodFromRecent(Long userId, Gender gender) {
        log.info("[RecommendVectorFacade] 가장 가까운 음식 찾기 시작: userId={}, gender={}", userId, gender);

        try {
            List<Long> recentFoodIds = loadRecentUserFoodIds(userId);
            List<Long> recentRecommendedFoodIds = loadRecentRecommendedFoodIds(userId);

            if (recentFoodIds.size() < MIN_RECENT_FOODS) {
                log.warn("[RecommendVectorFacade] 최근 음식 데이터 부족: userId={}, 최근 음식 수={}",
                        userId, recentFoodIds.size());
                throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
            }

            double[] avgVector = redisVectorSearcher.computeAverageVector(recentFoodIds, gender);
            if (!isValidVector(avgVector)) {
                log.error("[RecommendVectorFacade] 평균 벡터 계산 실패: userId={}", userId);
                throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
            }

            List<Long> results = redisVectorSearcher.knnSearch(gender, avgVector, SEARCH_CANDIDATE_SIZE);
            Long recommendedFoodId = checkDuplicatedRecommend(results, recentFoodIds, recentRecommendedFoodIds);

            log.info("[RecommendVectorFacade] 가장 가까운 음식 찾기 완료: userId={}, foodId={}", userId, recommendedFoodId);
            return recommendedFoodId;

        } catch (HankkiWikiException e) {
            throw e;
        } catch (Exception e) {
            log.error("[RecommendVectorFacade] 가장 가까운 음식 찾기 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
        }
    }

    public List<Long> loadRecentUserFoodIds(Long userId) {
        log.debug("[RecommendVectorFacade] 최근 음식 ID 로딩 시작: userId={}", userId);

        try {
            LocalDate today = LocalDate.now();
            LocalDate threeDaysAgo = today.minusDays(2);

            // 1) 최근 3일간 DietGroup ID 조회
            List<Long> groupIds = dietGroupRepository
                    .findIdsByUserIdAndTakeAtBetween(userId, threeDaysAgo, today);

            if (groupIds.isEmpty()) {
                log.info("[RecommendVectorFacade] 최근 3일간 식단 그룹이 없습니다: userId={}", userId);
                return Collections.emptyList();
            }

            // 2) 그 그룹들에 속한 Food ID 조회
            List<Long> foodIds = dietFoodRepository.findFoodIdsByDietGroupIdIn(groupIds);

            log.info("[RecommendVectorFacade] 최근 음식 ID 로딩 완료: userId={}, 그룹 수={}, 음식 수={}",
                    userId, groupIds.size(), foodIds.size());
            return foodIds;

        } catch (Exception e) {
            log.error("[RecommendVectorFacade] 최근 음식 ID 로딩 실패: userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<Long> loadRecentRecommendedFoodIds(Long userId) {
        log.debug("[RecommendVectorFacade] 최근 추천 음식 ID 로딩 시작: userId={}", userId);

        try {
            LocalDate today = LocalDate.now();
            LocalDate threeDaysAgo = today.minusDays(2);

            List<Long> recommendedIds = userFoodLogRepository.findRecommendedFoodIdsByUserIdAndTakeAtBetween(
                    userId, threeDaysAgo, today);

            log.info("[RecommendVectorFacade] 최근 추천 음식 ID 로딩 완료: userId={}, 추천 수={}",
                    userId, recommendedIds.size());
            return recommendedIds;

        } catch (Exception e) {
            log.error("[RecommendVectorFacade] 최근 추천 음식 ID 로딩 실패: userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private boolean isValidVector(double[] vector) {
        if (vector == null || vector.length != EXPECTED_VECTOR_DIMENSION) {
            log.error("[RecommendVectorFacade] 벡터 차원 오류: 예상={}, 실제={}",
                    EXPECTED_VECTOR_DIMENSION, (vector == null ? "null" : vector.length));
            return false;
        }
        return true;
    }

    private Long checkDuplicatedRecommend(
            List<Long> foodIds,
            List<Long> recentTakenFoodIds,
            List<Long> recentRecommendedFoodIds
    ) {
        if (foodIds == null || foodIds.isEmpty()) {
            log.warn("[RecommendVectorFacade] 추천 후보군이 비어 있습니다. (벡터 검색 실패 또는 Redis 문제)");
            throw new IllegalStateException("추천할 음식이 없습니다.");
        }

        Set<Long> recent = new HashSet<>();
        recent.addAll(recentTakenFoodIds);
        recent.addAll(recentRecommendedFoodIds);

        log.debug("[RecommendVectorFacade] 중복 확인: 후보 수={}, 제외할 음식 수={}",
                foodIds.size(), recent.size());

        Long recommendedFood = foodIds.stream()
                .filter(id -> !recent.contains(id))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("[RecommendVectorFacade] 모든 후보가 최근 섭취 또는 추천 목록에 포함됨. " +
                            "후보 수={}, 제외 수={}", foodIds.size(), recent.size());
                    return new IllegalStateException("중복을 제외한 추천 후보가 없습니다.");
                });

        log.info("[RecommendVectorFacade] 중복 검사 완료: 추천 음식 ID={}", recommendedFood);
        return recommendedFood;
    }
}
