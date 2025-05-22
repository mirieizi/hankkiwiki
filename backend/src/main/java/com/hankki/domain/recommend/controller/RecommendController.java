package com.hankki.domain.recommend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hankki.common.security.principal.CurrentUser;
import com.hankki.domain.auth.dto.UserPrincipal;
import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.recommend.service.RecommendFacade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
@Tag(name = "음식 추천 API", description = "벡터 기반 추천/랜덤 추천 기능 제공")
public class RecommendController {

    private final RecommendFacade recommendFacade;

    @Operation(summary = "무작위 음식 추천", description = "무작위로 하나의 음식을 추천합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "200", description = "추천 성공", content = @Content),
                    @ApiResponse(responseCode = "429", description = "추천 가능 횟수 초과"),
                    @ApiResponse(responseCode = "404", description = "추천할 음식 없음")
            })
    @GetMapping("/random")
    public ResponseEntity<FoodResponseDto> recommendRandom(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(recommendFacade.recommendRandom(user.getUserId(), user.getGender()));
    }

    @Operation(summary = "유사한 음식 추천", description = "최근 섭취한 음식과 가장 유사한 음식을 추천합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 성공", content = @Content),
            @ApiResponse(responseCode = "429", description = "추천 가능 횟수 초과"),
            @ApiResponse(responseCode = "404", description = "추천할 음식 없음 또는 최근 섭취 내역 없음")
    })
    @GetMapping("/similar")
    public ResponseEntity<FoodResponseDto> recommendSimilar(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(recommendFacade.recommendMostSimilar(user.getUserId(), user.getGender()));
    }

    @Operation(summary = "중립 음식 추천", description = "최근 섭취한 음식 벡터 평균과 중간 정도의 유사도를 가진 음식을 추천합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 성공", content = @Content),
            @ApiResponse(responseCode = "429", description = "추천 가능 횟수 초과"),
            @ApiResponse(responseCode = "404", description = "추천할 음식 없음 또는 최근 섭취 내역 없음")
    })
    @GetMapping("/neutral")
    public ResponseEntity<FoodResponseDto> recommendNeutral(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(recommendFacade.recommendNeutral(user.getUserId(), user.getGender()));
    }

    @Operation(summary = "가장 먼 음식 추천", description = "최근 섭취한 음식 벡터 평균과 가장 유사하지 않은(거리가 먼) 음식을 추천합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 성공", content = @Content),
            @ApiResponse(responseCode = "429", description = "추천 가능 횟수 초과"),
            @ApiResponse(responseCode = "404", description = "추천할 음식 없음 또는 최근 섭취 내역 없음")
    })
    @GetMapping("/furthest")
    public ResponseEntity<FoodResponseDto> recommendFurthest(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(recommendFacade.recommendFurthest(user.getUserId(), user.getGender()));
    }
    
    @Operation(summary = "AI RAG 기반 음식 추천", description = "최근 섭취한 음식과, 건강정보를 바탕으로 RAG 기법으로 음식을 추천합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "추천 성공", content = @Content),
        @ApiResponse(responseCode = "429", description = "추천 가능 횟수 초과"),
        @ApiResponse(responseCode = "404", description = "추천할 음식 없음 또는 최근 섭취 내역 없음")
    })
    @PostMapping("/rag")
    public ResponseEntity<FoodResponseDto> recommendByRag(
            @CurrentUser UserPrincipal user
    ) {
        FoodResponseDto response = recommendFacade.recommendByRag(user.getUserId(), null);
        return ResponseEntity.ok(response);
    }

}
