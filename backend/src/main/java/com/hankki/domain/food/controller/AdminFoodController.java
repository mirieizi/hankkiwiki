package com.hankki.domain.food.controller;

import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/food/admin")
@RequiredArgsConstructor
public class AdminFoodController {

    private final FoodRepository foodRepository;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

}
