package com.hankki.domain.diet.dto;

import com.hankki.domain.food.dto.FoodGroupDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자의 식사 일자에 대한 모든 식단 응답 DTO")
public class GroupedDietResponseDto {

    @NotBlank
    @Schema(description = "식사 일자", example = "2025-05-28")
    private LocalDate takeAt;

    @Schema(description = "식사 일자에 대한 모든 음식들", implementation = FoodGroupDto.class)
    private List<FoodGroupDto> foods;
}
