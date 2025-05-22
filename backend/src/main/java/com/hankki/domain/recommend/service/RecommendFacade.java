package com.hankki.domain.recommend.service;

import org.springframework.stereotype.Service;

import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.recommend.dto.RagRecommendRequest;
import com.hankki.domain.user.constant.Gender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendFacade {

    private final RecommendVectorFacade recommendVectorFacade;
    private final RecommendService recommendService;
    private UserLogServiceImpl userLogService;

    public FoodResponseDto recommendRandom(Long userId, Gender gender) {
        userLogService.checkQuota(userId); // 남은 횟수 체크
        FoodResponseDto foodResponseDto = recommendService.recommendRandomFood(gender);
        userLogService.recordRecommendation(userId); // 추천 횟수 기록
        return foodResponseDto;
    }

    public FoodResponseDto recommendFurthest(Long userId, Gender gender) {
        userLogService.checkQuota(userId);
        Long furthestId = recommendVectorFacade.findFurthestFoodFromRecent(userId, gender);
        userLogService.recordRecommendation(userId);
        return recommendService.findFoodDtoById(furthestId);
    }

    public FoodResponseDto recommendNeutral(Long userId, Gender gender) {
        userLogService.checkQuota(userId);
        Long neutralId = recommendVectorFacade.findNeutralFoodFromRecent(userId, gender);
        userLogService.recordRecommendation(userId);
        return recommendService.findFoodDtoById(neutralId);
    }

    public FoodResponseDto recommendMostSimilar(Long userId, Gender gender) {
        userLogService.checkQuota(userId);
        Long mostSimilarId = recommendVectorFacade.findMostSimilarFoodFromRecent(userId, gender);
        userLogService.recordRecommendation(userId);
        return recommendService.findFoodDtoById(mostSimilarId);
    }

	public FoodResponseDto recommendByRag(Long userId, RagRecommendRequest request) {
		return recommendService.recommendByRag(userId, request);
	}

}
