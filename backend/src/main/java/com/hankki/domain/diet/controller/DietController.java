package com.hankki.domain.diet.controller;

import java.time.LocalDate;

import com.hankki.common.security.principal.CurrentUser;
import com.hankki.domain.diet.dto.*;
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
import com.hankki.domain.diet.service.DietFacade;
import com.hankki.domain.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {

    private final DietFacade dietFacade;

    @Operation(summary = "식단 생성", description = "사용자의 식단(Diet)을 생성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "식단 생성을 성공하였습니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당하는 사용자를 찾지 못했습니다.")
    })
    @PostMapping
    public ResponseEntity<String> createDiet(
            @CurrentUser AuthUser authUser,
            @RequestBody DietCreateRequestDto requestDto
    ) {
        dietFacade.createDiet(authUser.getUser().getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Diet 생성 성공 응답");
    }

    @Operation(summary = "특정 날짜의 식단 조회", description = "특정 날짜(takeAt)의 사용자의 식단을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식단 조회에 성공하였습니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당하는 사용자를 찾지 못했습니다.")
    })
    @GetMapping("/get-by-date")
    public ResponseEntity<GroupedDietResponseDto> getDietsByDate(
            @CurrentUser AuthUser authUser,
            @RequestBody DietGetByTakeAtRequestDto requestDto
    ) {
        GroupedDietResponseDto responseDto = dietFacade.getDietsByDate(
        		authUser.getUser().getId(),
                requestDto.getTakeAt());
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "특정 사용자의 식단 수정", description = "특정 사용자의 식단을 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식단을 수정하였습니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당하는 사용자를 찾지 못했습니다.")
    })
    @PatchMapping("/update/meal-type")
    public ResponseEntity<String> updateDietInfo(
            @CurrentUser AuthUser authUser,
            @RequestBody DietUpdateMealTypeRequestDto requestDto
    ){
        dietFacade.updateMealType(authUser.getUser().getId(), requestDto);
        return ResponseEntity.ok("Diet 정보 수정 성공");
    }

    @Operation(summary = "특정 식단 삭제", description = "현재 사용자의 특정 식단을 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식단을 삭제하였습니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당하는 사용자를 찾지 못했습니다.")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteDiet(
            @CurrentUser AuthUser authUser,
            @RequestBody DietDeleteRequestDto requestDto
    ){
        dietFacade.deleteDietById(authUser.getUser().getId(), requestDto.getDietId());
        return ResponseEntity.ok("Diet 삭제 성공");
    }

    /*********************************
     *      admin API 관리 구역        *
     *********************************/

    @Operation(summary = "특정 사용자의 식단 조회", description = "특정 사용자의 모든 식단을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식단 조회에 성공하였습니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당하는 사용자를 찾지 못했습니다.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/diet/admin/{userId}")
    public ResponseEntity<List<DietResponseDto>> getDietByAdmin(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(dietFacade.getDietsByUserId(userId));
    }

    @Operation(summary = "특정 사용자의 식단 수정", description = "특정 사용자의 식단을 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식단을 수정하였습니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당하는 사용자를 찾지 못했습니다.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{userId}/{dietId}")
    public ResponseEntity<String> updateDietByAdmin(
            @PathVariable Long userId,
            @PathVariable Long dietId,
            @RequestBody DietUpdateRequestDto requestDto
    ){
        dietFacade.updateDietInfo(userId, dietId, requestDto);
        return ResponseEntity.ok("Diet 정보 수정 성공");
    }

    @Operation(summary = "특정 식단 삭제", description = "현재 사용자의 특정 식단을 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식단을 삭제하였습니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당하는 사용자를 찾지 못했습니다.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{userId}/{dietId}")
    public ResponseEntity<String> deleteDietByAdmin(
            @PathVariable Long userId,
            @PathVariable Long dietId
    ){
        dietFacade.deleteDietByIdByAdmin(userId, dietId);
        return ResponseEntity.ok("Diet 삭제 성공");
    }

}
