package com.hankki.domain.diet.controller;

import com.hankki.domain.diet.dto.*;
import com.hankki.domain.diet.service.DietFacade;
import com.hankki.domain.diet.service.DietService;
import com.hankki.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {

    private final DietFacade dietFacade;

    @Operation(summary = "Diet 객체 생성", description = "사용자의 식단(Diet)를 생성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "식단 생성을 성공하였습니다.", content = @Content)
            @ApiResponse(responseCode = "404", description = "해당하는 사용자를 찾지 못했습니다.", )
    })
    @PostMapping
    public ResponseEntity<String> createDiet(
            @AuthenticationPrincipal User userDetails,
            @RequestBody DietCreateRequestDto requestDto
    ) {
        dietFacade.createDiet(userDetails.getUsername(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Diet 생성 성공 응답");
    }

    @GetMapping("/get-by-date")
    public ResponseEntity<GroupedDietResponseDto> getDietsByDate(
            @AuthenticationPrincipal User userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate takeAt
    ) {
        GroupedDietResponseDto responseDto = dietFacade.getDietsByDate(
                userDetails.getUsername(),
                takeAt);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteDiet(
            @AuthenticationPrincipal User userDetails,
            @RequestParam Long dietId
    ){
        dietFacade.deleteDietById(userDetails.getUsername(), dietId);
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
