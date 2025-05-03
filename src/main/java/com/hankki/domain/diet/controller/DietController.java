package com.hankki.domain.diet.controller;

import com.hankki.domain.diet.dto.DietResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/diet")
public class DietController {

    @PostMapping("/create")
    public ResponseEntity<DietResponseDto> createDiet() {
        return null;
    }
}
