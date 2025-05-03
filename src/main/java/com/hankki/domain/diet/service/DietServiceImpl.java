package com.hankki.domain.diet.service;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietGetByTakeAtRequestDto;
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

    @Override
    public DietResponseDto getDietByTakeAt(DietGetByTakeAtRequestDto requestDto) {
        log.info("[DietService] 회원의 해당 일자별 Diet 조회 Request : {}", requestDto);

        return null;
    }
}
