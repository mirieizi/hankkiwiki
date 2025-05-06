package com.hankki.domain.diet.controller;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietGetByDateRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;
import com.hankki.domain.diet.service.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {

    private final DietService dietService;

    @PostMapping("/create")
    public ResponseEntity<String> createDiet(@RequestBody DietCreateRequestDto requestDto) {
        dietService.createDiet(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Diet 생성 성공 응답");
    }

    @GetMapping("/find")
    public ResponseEntity<DietResponseDto> findDiet(@RequestBody DietGetByDateRequestDto requestDto) {

        return null;
    }
}
