package com.hankki.domain.diet.controller;

import com.hankki.domain.diet.dto.*;
import com.hankki.domain.diet.service.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {

    private final DietService dietService;

    @PostMapping
    public ResponseEntity<String> createDiet(
            @RequestBody DietCreateRequestDto requestDto
    ) {
        dietService.createDiet(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Diet 생성 성공 응답");
    }

    @GetMapping
    public ResponseEntity<List<DietResponseDto>> getDietsByDate(
            @RequestBody DietGetByTakeAtRequestDto requestDto
    ) {
        List<DietResponseDto> dietResponseDtos = dietService.getDietsByTakeAt(
                requestDto.getEmail(),
                requestDto.getTakeAt());
        return ResponseEntity.ok(dietResponseDtos);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteDiet(
            //@AuthenticationPrincipal UserDetails userDetails, 추가하기
            @RequestBody DietDeleteRequestDto requestDto
    ){
        dietService.deleteDietById(requestDto.getDietId());
        return ResponseEntity.ok("Diet 삭제 성공");
    }

    @PatchMapping("/update/diet-info")
    public ResponseEntity<String> updateDietInfo(
            @RequestBody DietUpdateInfoRequestDto requestDto
    ){
        dietService.updateDietInfo(requestDto);
        return ResponseEntity.ok("Diet 정보 수정 성공");
    }

}
