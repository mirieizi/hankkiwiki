package com.hankki.domain.recommend.service;

import jakarta.transaction.Transactional;
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
    private final UserLogServiceImpl userLogService;
    private final UserFoodLogServiceImpl userFoodLogService;

    /**
     * 랜덤 추천 기능
     * 남은 횟수 체크 -> 랜덤 뽑기 -> 추천 횟수 기록
     * @param userId
     * @param gender
     * @return
     */
    @Transactional
    public FoodResponseDto recommendRandom(Long userId, Gender gender) {
        userLogService.checkQuota(userId);
        FoodResponseDto foodResponseDto = recommendService.recommendRandomFood(gender);
        /**
         * TO DO (1) 랜덤 추천 불가 시 대처 방식 : 다시 시도, 횟수 돌려놓기 등
         */
        userLogService.recordRecommendation(userId);
        userFoodLogService.createUserFoodLog(userId, foodResponseDto.getId());
        return foodResponseDto;
    }

    /**
     * 3일 간 식사에서 벡터로 거리가 가장 먼 음식 추천
     * 남은 횟수 체크 -> 가장 먼 음식 선택 -> 추천 횟수에 저장
     * @param userId
     * @param gender
     * @return
     */
    public FoodResponseDto recommendFurthest(Long userId, Gender gender) {
        userLogService.checkQuota(userId);
        Long furthestId = recommendVectorFacade.findFurthestFoodFromRecent(userId, gender);
        FoodResponseDto foodResponseDto = recommendService.findFoodDtoById(furthestId);
        userLogService.recordRecommendation(userId);
        userFoodLogService.createUserFoodLog(userId, foodResponseDto.getId());
        return foodResponseDto;
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
        FoodResponseDto foodResponseDto =  recommendService.findFoodDtoById(neutralId);
        userLogService.recordRecommendation(userId);
        userFoodLogService.createUserFoodLog(userId, foodResponseDto.getId());
        return foodResponseDto;
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
        FoodResponseDto foodResponseDto = recommendService.findFoodDtoById(mostSimilarId);
        userLogService.recordRecommendation(userId);
        userFoodLogService.createUserFoodLog(userId, foodResponseDto.getId());
        return foodResponseDto;
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
