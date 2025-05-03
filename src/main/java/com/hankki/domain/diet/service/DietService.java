package com.hankki.domain.diet.service;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;

public interface DietService {

    DietResponseDto createDiet(DietCreateRequestDto requestDto);
}
