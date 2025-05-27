package com.hankki.domain.recommend.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.recommend.entity.UserLog;
import com.hankki.domain.recommend.repository.UserLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserLogServiceImpl {

    private final UserLogRepository userLogRepository;
    private static final int MAX_RECOMMEND_COUNT_PER_DAY = 500;

    /**
     * 추천 횟수 초과 여부 확인
     * @param userId
     */
    @Transactional
    public void checkQuota(Long userId) {
        LocalDate today = LocalDate.now();
        UserLog userLog = userLogRepository.findByUserIdAndDate(userId, today).orElse(null);

        if (userLog != null && userLog.isExceeded(MAX_RECOMMEND_COUNT_PER_DAY)) {
            throw new HankkiWikiException(ExceptionStatus.RECOMMEND_QUOTA_EXCEEDED);
        }
    }
    
    public int checkSpoon(Long userId) {
        LocalDate today = LocalDate.now();
        // 오늘의 기록을 찾는다
        UserLog userLog = userLogRepository.findByUserIdAndDate(userId, today).orElse(null);
        int used = (userLog != null) ? userLog.getRecommendationCount() : 0;
        // 남은 스푼 반환 (최대에서 사용한 횟수 뺀 값)
        return Math.max(0, MAX_RECOMMEND_COUNT_PER_DAY - used);
    }
    

    /**
     * 추천 시도 기록 (없으면 생성, 있으면 +1)
     */
    @Transactional
    public void recordRecommendation(Long userId) {
        LocalDate today = LocalDate.now();
        UserLog userLog = userLogRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> UserLog.builder()
                        .userId(userId)
                        .date(today)
                        .recommendationCount(0)
                        .build());

        userLog.increase();
        userLogRepository.save(userLog);
    }
}
