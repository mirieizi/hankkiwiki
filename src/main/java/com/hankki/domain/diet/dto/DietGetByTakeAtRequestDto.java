package com.hankki.domain.diet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DietGetByTakeAtRequestDto {

    private String email;

    private String takeAt;
}
