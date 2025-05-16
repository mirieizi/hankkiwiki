package com.hankki.domain.diet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "특정 식단 삭제 요청 DTO")
public class DietDeleteRequestDto {

    @NotBlank
    @Schema(description = "식단 Id", example = "1")
    private Long dietId;
}
