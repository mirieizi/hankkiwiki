package com.hankki.domain.diary.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hankki.domain.diary.dto.DiaryCreateRequest;
import com.hankki.domain.diary.dto.DiaryResponse;
import com.hankki.domain.diary.dto.DiaryUpdateRequest;
import com.hankki.domain.diary.service.DiaryService;
import com.hankki.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * 다이어리 컨트롤러
 */
@RestController
@RequestMapping("/user/diaries")
@RequiredArgsConstructor
@Validated
public class DiaryController {
    private final DiaryService diaryService;

    /**
     * 일기 작성
     * POST /user/diaries
     */
    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Long> createDiary(
        @AuthenticationPrincipal User user,
        @RequestBody DiaryCreateRequest request
    ) {
        Long savedId = diaryService.createDiary(user.getId(), request);
        return ResponseEntity.ok(savedId);
    }


    /**
     * 특정 일기 조회
     * GET /user/diaries/{diaryId}
     */
    @GetMapping("/{diaryId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<DiaryResponse> getDiaryById(
        @AuthenticationPrincipal User user,
        @PathVariable Long diaryId
    ) {
        DiaryResponse response = diaryService.getDiaryById(user.getId(), diaryId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 일기 수정
     * PUT /user/diaries/{diaryId}
     */
    @PutMapping("/{diaryId}")
    @Transactional
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<DiaryResponse> updateDiary(
        @AuthenticationPrincipal User user,
        @PathVariable Long diaryId,
        @RequestBody DiaryUpdateRequest request
    ) {
        DiaryResponse updated = diaryService.updateDiary(user.getId(), diaryId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 특정 일기 삭제
     * DELETE /user/diaries/{diaryId}
     */
    @DeleteMapping("/{diaryId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteDiary(
        @AuthenticationPrincipal User user,
        @PathVariable Long diaryId
    ) {
        diaryService.deleteDiary(user.getId(), diaryId);
        return ResponseEntity.noContent().build();
    }



    /**
     * 관리자: 특정 사용자 일기 조회
     * GET /admin/diaries/{userId}/{diaryId}
     */
    @GetMapping(path = "/admin/diaries/{userId}/{diaryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiaryResponse> getDiaryByUserAdmin(
        @PathVariable Long userId,
        @PathVariable Long diaryId
    ) {
        DiaryResponse response = diaryService.getDiaryById(userId, diaryId);
        return ResponseEntity.ok(response);
    }

    /**
     * 관리자: 특정 사용자 일기 수정
     * PUT /admin/diaries/{userId}/{diaryId}
     */
    @PutMapping(path = "/admin/diaries/{userId}/{diaryId}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiaryResponse> updateDiaryAdmin(
        @PathVariable Long userId,
        @PathVariable Long diaryId,
        @RequestBody DiaryUpdateRequest request
    ) {
        DiaryResponse updated = diaryService.updateDiary(userId, diaryId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 관리자: 특정 사용자 일기 삭제
     * DELETE /admin/diaries/{userId}/{diaryId}
     */
    @DeleteMapping(path = "/admin/diaries/{userId}/{diaryId}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDiaryAdmin(
        @PathVariable Long userId,
        @PathVariable Long diaryId
    ) {
        diaryService.deleteDiary(userId, diaryId);
        return ResponseEntity.noContent().build();
    }
}
