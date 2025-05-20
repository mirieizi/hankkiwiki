package com.hankki.domain.recommend.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.user.constant.Gender;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final FoodRepository foodRepository;

    /**
     * 무작위 음식 1개 추천
     */
    @Transactional
    @Override
    public FoodResponseDto recommendRandomFood(Gender gender) {
        Food food = foodRepository.findRandomFood()
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_FOOD));
        return FoodResponseDto.fromEntity(food);
    }

    /**
     * 음식 ID로 조회 후 DTO 변환
     */
    @Transactional
    @Override
    public FoodResponseDto findFoodDtoById(Long foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new IllegalStateException("해당 음식이 존재하지 않습니다."));
        return FoodResponseDto.fromEntity(food);
    }

}
