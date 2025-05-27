package com.hankki.domain.recommend.service;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.recommend.entity.UserFoodLog;
import com.hankki.domain.recommend.repository.UserFoodLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserFoodLogServiceImpl {

    private final UserFoodLogRepository userFoodLogRepository;

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


}
