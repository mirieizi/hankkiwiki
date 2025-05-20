package com.hankki.domain.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hankki.common.security.principal.CurrentUser;
import com.hankki.domain.auth.dto.UserPrincipal;
import com.hankki.domain.user.dto.DailyCalorieResponse;
import com.hankki.domain.user.dto.UpdateUserHealthRequest;
import com.hankki.domain.user.dto.UserHealthInfoResponse;
import com.hankki.domain.user.dto.UserHealthRequest;
import com.hankki.domain.user.entity.UserHealthInfo;
import com.hankki.domain.user.service.UserHealthService;

import lombok.RequiredArgsConstructor;

/**
 * 일반 사용자 건강정보 컨트롤러
 */
@RestController
@RequestMapping("/user/me")
@RequiredArgsConstructor
@Validated
public class UserHealthController {
    private static final Logger log = LoggerFactory.getLogger(UserHealthController.class);
    private final UserHealthService userHealthService;

    /**
     * 1) 일반 사용자: 자신의 건강정보 등록
     * POST /user/me/health
     */
    @PostMapping("/health")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Long> registerOwnHealth(@CurrentUser UserPrincipal principal,
                                                  @Validated @RequestBody UserHealthRequest request) {
        Long userId = principal.getUserId();
        log.info("Register health for self, userId={}", userId);
        Long savedId = userHealthService.registerHealthInfo(userId, request);
        return ResponseEntity.ok(savedId);
    }

    /**
     * 2) 자신의 건강정보 조회
     * GET /user/me/health
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserHealthInfoResponse> getOwnHealth(@CurrentUser UserPrincipal principal) {
        Long userId = principal.getUserId();
        UserHealthInfoResponse info = userHealthService.findHealthById(userId);
        return ResponseEntity.ok(info);
    }

    /**
     * 3) 자신의 건강정보 수정
     * PATCH /user/me/health
     */
    @PatchMapping("/health")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserHealthInfo> updateOwnHealth(@CurrentUser UserPrincipal principal,
                                                          @Validated @RequestBody UpdateUserHealthRequest request) {
        Long userId = principal.getUserId();
        UserHealthInfo updated = userHealthService.updateHealthInfo(userId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 4) 자신의 건강정보 삭제
     * DELETE /user/me/health
     */
    @DeleteMapping("/health")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOwnHealth(@CurrentUser UserPrincipal principal) {
        Long userId = principal.getUserId();
        userHealthService.deleteHealthInfo(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 5) 일일 권장 칼로리 조회
     * GET /user/me/health/dailycalorie
     */
    @GetMapping("/health/dailycalorie")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DailyCalorieResponse> getUserDailyCalorie(@CurrentUser UserPrincipal principal){
        Long userId = principal.getUserId();
        DailyCalorieResponse calorie =  userHealthService.getUserDailyCalorie(userId);
        return ResponseEntity.ok(calorie);
    }
}
