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

    /**
     * 랜덤 추천 기능
     * @param userId
     * @param gender
     * @return
     */
    public FoodResponseDto recommendRandom(Long userId, Gender gender) {
        userLogService.checkQuota(userId); // 남은 횟수 체크
        FoodResponseDto foodResponseDto = recommendService.recommendRandomFood(gender);
        userLogService.recordRecommendation(userId); // 추천 횟수 기록
        return foodResponseDto;
    }

    /**
     * 3일 간 식사에서 벡터로 거리가 가장 먼 음식 추천
     * @param userId
     * @param gender
     * @return
     */
    public FoodResponseDto recommendFurthest(Long userId, Gender gender) {
        userLogService.checkQuota(userId);
        Long furthestId = recommendVectorFacade.findFurthestFoodFromRecent(userId, gender);
        userLogService.recordRecommendation(userId);
        return recommendService.findFoodDtoById(furthestId);
    }

    /**
     * 3일 간 식단에서 벡터로 거리가 가까운 것과 먼 것의 중간 값 추천
     * @param userId
     * @param gender
     * @return
     */
    public FoodResponseDto recommendNeutral(Long userId, Gender gender) {
        userLogService.checkQuota(userId);
        Long neutralId = recommendVectorFacade.findNeutralFoodFromRecent(userId, gender);
        userLogService.recordRecommendation(userId);
        return recommendService.findFoodDtoById(neutralId);
    }

    /**
     * 3일 간 식단에서 벡터로 거리가 가장 가까운 음식 추천
     * @param userId
     * @param gender
     * @return
     */
    public FoodResponseDto recommendMostSimilar(Long userId, Gender gender) {
        userLogService.checkQuota(userId);
        Long mostSimilarId = recommendVectorFacade.findMostSimilarFoodFromRecent(userId, gender);
        userLogService.recordRecommendation(userId);
        return recommendService.findFoodDtoById(mostSimilarId);
    }

    /**
     * RAG를 활용한 AI 음식 추천
     * @param userId
     * @param request
     * @return
     */
	public FoodResponseDto recommendByRag(Long userId, RagRecommendRequest request) {
        // TO DO: 추천 횟수 체크 여부
		return recommendService.recommendByRag(userId, request);
	}

}
