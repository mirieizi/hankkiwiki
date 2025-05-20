package com.hankki.domain.recommend.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.user.constant.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final FoodRepository foodRepository;

    @Override
    public FoodResponseDto recommendRandomFood(Gender gender) {
        Food food = foodRepository.findRandomFood()
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_FOOD));
        return FoodResponseDto.fromEntity(food);
    }
}
