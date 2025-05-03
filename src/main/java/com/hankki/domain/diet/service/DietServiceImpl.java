package com.hankki.domain.diet.service;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;
import com.hankki.domain.diet.entity.Diet;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DietServiceImpl implements DietService {

    @Transactional
    @Override
    public DietResponseDto createDiet(DietCreateRequestDto requestDto) {
        log.info("[DietService] Diet 생성 Request : {}", requestDto);

        /*
        TO-DO: 중복, 유효 검사
         */
        Diet createdDiet = requestDto.toEntity();

        return new DietResponseDto(createdDiet);
    }
}
