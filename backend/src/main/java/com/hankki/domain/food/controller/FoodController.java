package com.hankki.domain.food.controller;

import com.hankki.common.security.principal.CurrentUser;
import com.hankki.domain.auth.dto.UserPrincipal;
import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.food.service.FoodQueryServiceImpl;
import com.hankki.domain.recommend.dto.FoodResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/food")
@RequiredArgsConstructor
@Tag(name = "음식 API", description = "음식 검색 및 조회 기능 제공")
public class FoodController {

    private final FoodRepository foodRepository;
    private final FoodQueryServiceImpl foodQueryService;

    @Operation(summary = "음식 검색", description = "정확 매칭 우선 후 풀텍스트 검색을 시도합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = FoodResponseDto.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<FoodResponseDto>> searchFoods(
            @Parameter(description = "검색어", example = "김치찌개")
            @RequestParam String query) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<FoodResponseDto> results = foodQueryService.searchFoods(query.trim());
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "음식 상세 조회", description = "음식 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = FoodResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "음식을 찾을 수 없음")
    })
    @GetMapping("/{foodId}")
    public ResponseEntity<FoodResponseDto> getFoodById(
            @Parameter(description = "음식 ID", example = "1")
            @PathVariable Long foodId) {

        return foodRepository.findById(foodId)
                .map(FoodResponseDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "랜덤 음식 조회", description = "무작위로 음식 하나를 조회합니다.")
    @GetMapping("/random")
    public ResponseEntity<FoodResponseDto> getRandomFood() {
        return foodRepository.findRandomFood()
                .map(FoodResponseDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "음식 미리보기 목록 조회", description = "여러 음식 ID로 미리보기 정보를 조회합니다.")
    @PostMapping("/previews")
    public ResponseEntity<List<FoodPreviewResponseDto>> getFoodPreviews(
            @RequestBody List<Long> foodIds) {

        if (foodIds == null || foodIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<FoodPreviewResponseDto> previews = foodQueryService.getFoodPreviews(foodIds);
        return ResponseEntity.ok(previews);
    }

    @Operation(summary = "사용자 최근 음식 조회", description = "최근 3일간 섭취한 음식 조회")
    @GetMapping("/recent")
    public ResponseEntity<List<FoodPreviewResponseDto>> getRecentFoods(@CurrentUser UserPrincipal principal) {
        List<Long> recentFoodIds = foodQueryService.findFoodsByUserIdAndTakeAtBetween(principal.getUserId());
        List<FoodPreviewResponseDto> recentFoods = foodQueryService.getFoodPreviews(recentFoodIds);
        return ResponseEntity.ok(recentFoods);
    }

    @Operation(summary = "음식 이름으로 정확히 찾기", description = "정확한 음식 이름으로 조회합니다.")
    @GetMapping("/name")
    public ResponseEntity<FoodResponseDto> getFoodByName(
            @RequestParam String foodName) {

        return foodRepository.findByFoodName(foodName)
                .map(FoodResponseDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*********************************
     *      admin API 관리 구역        *
     *********************************/

    @Operation(summary = "전체 음식 목록 조회 (관리자)", description = "관리자가 전체 음식 목록을 조회합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<FoodResponseDto>> getAllFoods() {
        List<Food> allFoods = foodRepository.findAll();
        List<FoodResponseDto> response = allFoods.stream()
                .map(FoodResponseDto::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}