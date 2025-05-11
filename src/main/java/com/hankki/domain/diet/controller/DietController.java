package com.hankki.domain.diet.controller;

import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietDeleteRequestDto;
import com.hankki.domain.diet.dto.DietGetByTakeAtRequestDto;
import com.hankki.domain.diet.dto.DietResponseDto;
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
        return ResponseEntity.ok("사용자의 식단 삭제를 성공했습니다.");
    }


}
