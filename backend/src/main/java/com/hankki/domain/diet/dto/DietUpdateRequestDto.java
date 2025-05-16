package com.hankki.domain.diet.dto;

import com.hankki.domain.diet.constant.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDate;

@Schema
@Getter
public class DietUpdateRequestDto {
    @NotBlank
    private Long dietId;

    @NotBlank
    private String email;

    private LocalDate takeAt;

    private MealType mealType;

}
