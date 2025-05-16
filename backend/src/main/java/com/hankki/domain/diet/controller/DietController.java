package com.hankki.domain.diet.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hankki.domain.auth.entity.AuthUser;
import com.hankki.domain.diet.dto.DietCreateRequestDto;
import com.hankki.domain.diet.dto.DietUpdateMealTypeRequestDto;
import com.hankki.domain.diet.dto.GroupedDietResponseDto;
import com.hankki.domain.diet.service.DietFacade;
import com.hankki.domain.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {

    private final DietFacade dietFacade;

    @Operation(summary = "Diet 객체 생성", description = "사용자의 식단(Diet)를 생성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "식단 생성을 성공하였습니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당하는 사용자를 찾지 못했습니다.")
    })
    @PostMapping
    public ResponseEntity<String> createDiet(
            @AuthenticationPrincipal AuthUser  authUser,
            @RequestBody DietCreateRequestDto requestDto
    ) {
        dietFacade.createDiet(authUser.getUsername(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Diet 생성 성공 응답");
    }

    @GetMapping("/get-by-date")
    public ResponseEntity<GroupedDietResponseDto> getDietsByDate(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate takeAt
    ) {
        GroupedDietResponseDto responseDto = dietFacade.getDietsByDate(
        		authUser.getUsername(),
                takeAt);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteDiet(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long dietId
    ){
        dietFacade.deleteDietById(authUser.getUsername(), dietId);
        return ResponseEntity.ok("Diet 삭제 성공");
    }

    @PatchMapping("/update/meal-type")
    public ResponseEntity<String> updateDietInfo(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DietUpdateMealTypeRequestDto requestDto
    ){
        dietFacade.updateMealType(authUser.getUsername(), requestDto);
        return ResponseEntity.ok("Diet 정보 수정 성공");
    }

}
