package com.hankki.domain.diary.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hankki.domain.auth.dto.UserPrincipal;
import com.hankki.domain.auth.util.CurrentUser;
import com.hankki.domain.diary.dto.DiaryCreateRequest;
import com.hankki.domain.diary.dto.DiaryResponse;
import com.hankki.domain.diary.dto.DiaryUpdateRequest;
import com.hankki.domain.diary.service.DiaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * 다이어리 컨트롤러
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class DiaryController {
    private final DiaryService diaryService;

    /**
     * 일기 작성
     * POST /api/user/me/diaries
     */
    @PostMapping("/me/diaries")
    @Transactional
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "내 다이어리 작성", description = "로그인한 사용자의 다이어리를 작성합니다.")
    @ApiResponses({
    	@ApiResponse(responseCode = "201", description = "다이어리 작성 성공", content = @Content),
    	@ApiResponse(responseCode = "400", description = "입력값 오류")
    })
    public ResponseEntity<Long> createDiary(
        @CurrentUser UserPrincipal principal,
        @RequestBody DiaryCreateRequest request
    ) {
        Long savedId = diaryService.createDiary(principal.getUserId(), request);
        return ResponseEntity.ok(savedId);
    }


    /**
     * 내 전체 일기 목록 조회 (페이징, 필터링)
     * GET /api/user/me/diaries
     */
    @GetMapping("/me/diaries")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "내 전체 일기 목록 조회", description = "로그인한 사용자의 전체 일기 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<DiaryResponse>> getMyDiaries(@CurrentUser UserPrincipal principal){
    	List<DiaryResponse> diaries = diaryService.getDiariesByUserId(principal.getUserId());
    	return ResponseEntity.ok(diaries);
    }
    
    
    
    /**
     * 특정 일기 조회
     * GET api/user/me/diareis/{diaryId}
     */
    @GetMapping("/me/diaries/{diaryId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "내 특정 일기 조회", description = "로그인 한 사용자의 특정 일기를 조회함")
    @ApiResponses({
    	@ApiResponse(responseCode = "200", description = "조회성공", content = @Content),
    	@ApiResponse(responseCode = "401", description = "인증실패")
    })
    public ResponseEntity<DiaryResponse> getDiaryById(
        @CurrentUser UserPrincipal principal,
        @PathVariable Long diaryId
    ) {
        DiaryResponse response = diaryService.getDiaryById(principal.getUserId(), diaryId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 일기 수정
     * PUT /api/user/me/diaries/{diaryId}
     */
    @PatchMapping("/me/diaries/{diaryId}")
    @Transactional
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "내 특정 일기 수정", description = "로그인한 사용자의 특정 일기를 수정합니다.")
    @ApiResponses({
    	@ApiResponse(responseCode = "200", description = "조회성공", content = @Content),
    	@ApiResponse(responseCode = "400", description = "입력값 오류"),
    	@ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<DiaryResponse> updateDiary(
        @CurrentUser UserPrincipal principal,
        @PathVariable Long diaryId,
        @RequestBody DiaryUpdateRequest request
    ) {
        DiaryResponse updated = diaryService.updateDiary(principal.getUserId(), diaryId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 특정 일기 삭제
     * DELETE api/user/me/user/diaries/{diaryId}
     */
    @DeleteMapping("/me/diaries/{diaryId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "내 특정 일기 삭제", description = "로그인한 사용자의 특정 일기를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<Void> deleteDiary(
        @CurrentUser UserPrincipal principal,
        @PathVariable Long diaryId
    ) {
        diaryService.deleteDiary(principal.getUserId(), diaryId);
        return ResponseEntity.noContent().build();
    }


    /**
     * 관리자: 특정 사용자 일기 조회
     * GET /api/user/admin/diaries/{userId}/{diaryId}
     */
    @GetMapping("/admin/diaries/{userId}/{diaryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "특정 사용자 일기 조회 (관리자)", description = "관리자가 특정 사용자의 일기를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<DiaryResponse> getDiaryByUserAdmin(
        @PathVariable Long userId,
        @PathVariable Long diaryId
    ) {
        DiaryResponse response = diaryService.getDiaryById(userId, diaryId);
        return ResponseEntity.ok(response);
    }

    /**
     * 관리자: 특정 사용자 일기 삭제
     * DELETE /api/user/admin/diaries/{userId}/{diaryId}
     */
    @DeleteMapping("/admin/diaries/{userId}/{diaryId}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "특정 사용자 일기 삭제 (관리자)", description = "관리자가 특정 사용자의 일기를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<Void> deleteDiaryAdmin(
        @PathVariable Long userId,
        @PathVariable Long diaryId
    ) {
        diaryService.deleteDiary(userId, diaryId);
        return ResponseEntity.noContent().build();
    }
}