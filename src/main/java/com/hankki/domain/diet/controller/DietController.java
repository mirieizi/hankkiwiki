package com.hankki.domain.diet.controller;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietGetByDateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;
import com.hankki.domain.diet.service.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {

    private final DietService dietService;

    /*
     * 우선 세부적인 화면 구성이 확실하지 않아 반환값으로 생성된 Diet도 반환하게 했습니다.
     */
    @PostMapping("/create")
    public ResponseEntity<String> createDiet(@RequestBody DietCreateRequestDto requestDto) {
        dietService.createDiet(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Diet 생성 성공 응답");
    }

    public ResponseEntity<DietResponseDto> getDiet(@RequestBody DietGetByDateRequestDto requestDto) {

        return null;
    }
}
