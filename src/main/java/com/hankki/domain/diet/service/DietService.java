package com.hankki.domain.diet.service;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietGetByTakeAtRequestDto;
import com.hankki.domain.diet.dto.DietUpdateInfoRequestDto;
import com.hankki.domain.diet.entity.Diet;

import java.time.LocalDate;
import java.util.List;

public interface DietService {

    void createDiet(String email, DietCreateRequestDto requestDto);

    List<Diet> getDietsByTakeAt(String email, LocalDate takeAt);

    void deleteDietById(String email, Long dietId);

    void updateDietInfo(DietUpdateInfoRequestDto requestDto);
}
