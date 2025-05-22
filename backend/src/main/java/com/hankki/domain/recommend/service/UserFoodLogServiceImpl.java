package com.hankki.domain.recommend.service;

import com.hankki.domain.food.service.FoodQueryServiceImpl;
import com.hankki.domain.recommend.entity.UserFoodLog;
import com.hankki.domain.recommend.repository.UserFoodLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserFoodLogServiceImpl {

    private final UserFoodLogRepository userFoodLogRepository;
    private final FoodQueryServiceImpl foodQueryService;


    /**
     * UserFoodLog 생성 - 사용자가 추천 받은 음식 기록 저장
     * @param userId
     * @param foodId
     */
    public void createUserFoodLog(Long userId, Long foodId) {
        userFoodLogRepository.save(
                UserFoodLog.builder()
                        .userId(userId)
                        .foodId(foodId)
                        .date(LocalDate.now())
                        .build()
        );
    }

    /**
     * 3일 내 섭취 음식과 추천 받은 음식 중복 제외하기
     * @param userId
     * @param foodIds
     * @return
     */
    public Long checkDuplicatedRecommend(Long userId, List<Long> foodIds) {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(2);

        // 최근 섭취 음식 + 추천 음식 조회
        List<Long> recentTakenFoodIds = foodQueryService.findFoodsByUserIdAndTakeAtBetween(userId);
        List<Long> recentRecommendedFoodIds
                = userFoodLogRepository.findAllFoodIdsByUserIdAndTakeAtBetween(userId, threeDaysAgo, today);

        Set<Long> recent = new HashSet<>();
        recent.addAll(recentTakenFoodIds);
        recent.addAll(recentRecommendedFoodIds);

        return foodIds.stream()
                .filter(id -> !recent.contains(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("중복을 제외한 추천 후보가 없습니다."));

    }


}
