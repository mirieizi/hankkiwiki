package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.entity.Diet;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "식단 생성 요청 DTO")
public class DietCreateRequestDto {

    @Schema(description = "식사 종류", defaultValue = "TODAY", example = "LUNCH")
    private MealType mealType;

    @NotBlank
    @Schema(description = "식사 일자", example = "2025-05-28")
    private LocalDate takeAt;

    @NotBlank
    @Schema(description = "식사 음식들", example = "[1, 2, 3]")
    private List<Long> foodIds;

    public Diet toEntity(Long userId) {
        return Diet.builder()
                .userId(userId)
                .takeAt(this.takeAt)
                .mealType(this.mealType)
                .build();
    }
}
