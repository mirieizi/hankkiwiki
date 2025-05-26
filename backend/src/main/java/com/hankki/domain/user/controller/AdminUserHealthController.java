package com.hankki.domain.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hankki.domain.user.dto.UpdateUserHealthRequest;
import com.hankki.domain.user.dto.UserHealthInfoResponse;
import com.hankki.domain.user.dto.UserHealthRequest;
import com.hankki.domain.user.entity.UserHealthInfo;
import com.hankki.domain.user.service.UserHealthService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 사용자 건강정보 컨트롤러
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Validated
public class AdminUserHealthController {
    private static final Logger log = LoggerFactory.getLogger(AdminUserHealthController.class);
    private final UserHealthService userHealthService;

    /**
     * 1) 관리자: 특정 사용자 건강정보 등록
     * POST /admin/user/health/{userId}
     */
    @PostMapping("/health/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> registerAnyHealth(@PathVariable Long userId,
                                                  @Validated @RequestBody UserHealthRequest request) {
        log.info("Admin register health for userId={}", userId);
        Long savedId = userHealthService.registerHealthInfo(userId, request);
        return ResponseEntity.ok(savedId);
    }

    /**
     * 2) 관리자: 특정 사용자 건강정보 조회
     * GET /admin/user/health/{userId}
     */
    @GetMapping("/health/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserHealthInfoResponse> getAnyHealth(@PathVariable Long userId) {
        UserHealthInfoResponse info = userHealthService.findHealthById(userId);
        return ResponseEntity.ok(info);
    }

    /**
     * 3) 관리자: 특정 사용자 건강정보 수정
     * PATCH /admin/user/health/{userId}
     */
    @PatchMapping("/health/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserHealthInfo> updateAnyHealth(@PathVariable Long userId,
                                                          @Validated @RequestBody UpdateUserHealthRequest request) {
        UserHealthInfo updated = userHealthService.updateHealthInfo(userId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 4) 관리자: 특정 사용자 건강정보 삭제
     * DELETE /admin/user/health/{userId}
     */
    @DeleteMapping("/health/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteAnyHealth(@PathVariable Long userId) {
        userHealthService.deleteHealthInfo(userId);
        return ResponseEntity.noContent().build();
    }
}
