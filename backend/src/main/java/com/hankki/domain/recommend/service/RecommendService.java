package com.hankki.domain.recommend.service;

import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.recommend.dto.RagRecommendRequest;
import com.hankki.domain.user.constant.Gender;

public interface RecommendService {
    FoodResponseDto recommendRandomFood(Gender gender);

    FoodResponseDto findFoodDtoById(Long foodId);
    
    FoodResponseDto recommendByRag(Long userId, RagRecommendRequest request);
}