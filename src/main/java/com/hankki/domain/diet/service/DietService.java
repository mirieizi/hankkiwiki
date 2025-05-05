package com.hankki.domain.diet.service;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietGetByDateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;

public interface DietService {

    void createDiet(DietCreateRequestDto requestDto);

    DietResponseDto getMealItemByTakeAtAndMealType(DietGetByDateRequestDto requestDto);
}
