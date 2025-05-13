package com.hankki.domain.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hankki.domain.user.dto.UpdateUserHealthRequest;
import com.hankki.domain.user.dto.UserHealthRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.entity.UserHealthInfo;
import com.hankki.domain.user.service.UserHealthService;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 건강정보 컨트롤러
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserHealthController {
    private static final Logger log = LoggerFactory.getLogger(UserHealthController.class);
    private final UserHealthService userHealthService;

    /**
         /**
     * 1) 일반 사용자: 자신의 건강정보 등록
     * POST /user/me/health
     */
    @PostMapping("/me/health")
    @Transactional
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Long> registerOwnHealth(
        @AuthenticationPrincipal User user,
        @Validated @RequestBody UserHealthRequest request
    ) {
        Long userId = user.getId();
        log.info("Register health for self, userId={}", userId);
        Long savedId = userHealthService.registerHealthInfo(userId, request);
        return ResponseEntity.ok(savedId);
    }

    /**
         /**
     * 2) 관리자: 모든 사용자의 건강정보 등록
     * POST /user/admin/health/{userId}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/health/{userId}")
    @Transactional
    public ResponseEntity<Long> registerAnyHealth(
        @PathVariable Long userId,
        @Validated @RequestBody UserHealthRequest request
    ) {
        log.info("Admin register health for userId={}", userId);
        Long savedId = userHealthService.registerHealthInfo(userId, request);
        return ResponseEntity.ok(savedId);
    }

    /**
         /**
     * 3) 조회
     * GET /user/me/health - 자신의 건강정보 조회
     * GET /user/admin/health/{userId} - 관리자가 사용자의 건강정보 조회
     */
    @GetMapping("/me/health")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserHealthInfo> getOwnHealth(
        @AuthenticationPrincipal User user
    ) {
        Long userId = user.getId();
        UserHealthInfo info = userHealthService.findHealthById(userId);
        return ResponseEntity.ok(info);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/health/{userId}")
    public ResponseEntity<UserHealthInfo> getAnyHealth(@PathVariable Long userId) {
        UserHealthInfo info = userHealthService.findHealthById(userId);
        return ResponseEntity.ok(info);
    }

    /**
         /**
     * 4) 수정
     * PUT /user/me/health - 자신의 건강정보 수정
     * PUT /user/admin/health/{userId} - 관리자가 사용자의 건강정보 수정
     */
    @PutMapping("/me/health")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserHealthInfo> updateOwnHealth(
        @AuthenticationPrincipal User user,
        @Validated @RequestBody UpdateUserHealthRequest request
    ) {
        Long userId = user.getId();
        UserHealthInfo updated = userHealthService.updateHealthInfo(userId, request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/health/{userId}")
    public ResponseEntity<UserHealthInfo> updateAnyHealth(
        @PathVariable Long userId,
        @Validated @RequestBody UpdateUserHealthRequest request
    ) {
        UserHealthInfo updated = userHealthService.updateHealthInfo(userId, request);
        return ResponseEntity.ok(updated);
    }

    /**
         /**
     * 5) 삭제
     * DELETE /user/me/health - 자신의 건강정보 삭제
     * DELETE /user/admin/health/{userId} - 관리자가 사용자의 건강정보 삭제
     */
    @DeleteMapping("/me/health")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOwnHealth(
        @AuthenticationPrincipal User user
    ) {
        Long userId = user.getId();
        userHealthService.deleteHealthInfo(userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/health/{userId}")
    public ResponseEntity<Void> deleteAnyHealth(@PathVariable Long userId) {
        userHealthService.deleteHealthInfo(userId);
        return ResponseEntity.noContent().build();
    }
}
