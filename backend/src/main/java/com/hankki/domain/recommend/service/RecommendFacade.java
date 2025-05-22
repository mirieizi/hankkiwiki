package com.hankki.domain.recommend.service;

import org.springframework.stereotype.Service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.recommend.dto.RagRecommendRequest;
import com.hankki.domain.user.constant.Gender;
import com.hankki.domain.user.repository.UserHealthInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendFacade {

    private final RecommendVectorFacade recommendVectorFacade;
    private final RecommendService recommendService;
    private UserHealthInfoRepository userHealthInfoRepository;
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
        userLogService.checkQuota(userId);
        try {
            FoodResponseDto response = recommendService.recommendByRag(userId, request);
            userLogService.recordRecommendation(userId);  // 추천 성공 후 기록
            return response;
        } catch (HankkiWikiException e) {
            // 추천 실패 시 fallback
            log.warn("RAG 추천 실패, fallback으로 최외곽 추천 실행: {}", e.getMessage());
            return recommendFurthest(userId, getGenderFromUserHealth(userId));
        }
    }

    // gender 추출 헬퍼 (request에 없으면 userId 기반 조회)
    private Gender getGenderFromUserHealth(Long userId) {
        return userHealthInfoRepository.findById(userId)
            .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER_HEALTH))
            .getGender();
    }

}
