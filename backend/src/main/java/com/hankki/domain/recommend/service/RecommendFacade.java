package com.hankki.domain.recommend.service;

import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.user.constant.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendFacade {

    private final RecommendService recommendService;
    private UserLogServiceImpl userLogService;

    public FoodResponseDto recommendRandom(Long userId, Gender gender) {
        userLogService.checkQuota(userId); // 남은 횟수 체크
        FoodResponseDto foodResponseDto = recommendService.recommendRandomFood(gender);
        userLogService.recordRecommendation(userId);
        return foodResponseDto;
    }
}
