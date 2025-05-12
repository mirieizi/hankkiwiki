package com.hankki.domain.diet.dto;

import com.hankki.domain.food.dto.FoodGroupDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GroupedDietResponseDto {
    private LocalDate takeAt;
    private List<FoodGroupDto> foods;
}
