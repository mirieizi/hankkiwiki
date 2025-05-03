package com.hankki.domain.diet.controller;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
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

    @PostMapping("/create")
    public ResponseEntity<DietResponseDto> createDiet(@RequestBody DietCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dietService.createDiet(requestDto));
    }
}
