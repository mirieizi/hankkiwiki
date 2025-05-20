package com.hankki.domain.food.controller;

import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin - 음식 관리", description = "관리자 전용 음식 데이터 조회 API")
@RestController
@RequestMapping("/food/admin")
@RequiredArgsConstructor
public class AdminFoodController {

    private final FoodRepository foodRepository;

    @Operation(summary = "음식 목록 전체 조회", description = "관리자 권한으로 전체 음식 데이터를 조회합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

}
