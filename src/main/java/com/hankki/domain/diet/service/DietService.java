package com.hankki.domain.diet.service;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;
import com.hankki.domain.diet.dto.DietUpdateInfoRequestDto;

import java.time.LocalDate;
import java.util.List;

public interface DietService {

    void createDiet(DietCreateRequestDto requestDto);

    List<DietResponseDto> getDietsByTakeAt(String email, LocalDate takeAt);

    void deleteDietById(Long dietId);

    void updateDietInfo(DietUpdateInfoRequestDto requestDto);
}
