package com.hankki.domain.food.repository;

import java.util.List;
import java.util.Optional;

import com.hankki.domain.food.entity.Food;

public interface FoodRepositoryCustom {
    Optional<Food> findBestMatchByTokens(List<String> tokens);
}
