package com.hankki.domain.user.controller;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hankki.domain.auth.dto.JwtTokenResponse;
import com.hankki.domain.auth.dto.LoginRequest;
import com.hankki.domain.auth.dto.SignUpRequest;
import com.hankki.domain.user.dto.UpdateUserRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 계정 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * 1) 회원 가입
     * POST /user/signup
     */
    @PostMapping("/signup")
    @Transactional
    @Operation(summary = "회원 가입", description = "이메일·비밀번호·닉네임으로 신규 회원을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원 생성 성공", content = @Content),
        @ApiResponse(responseCode = "400", description = "회원 생성 실패")
    })
    public ResponseEntity<Void> signup(@RequestBody SignUpRequest request) {
        log.info("Request to sign up user: {}", request.getEmail());
        Long newUserId = userService.signUp(request);
        URI location = URI.create("/user/" + newUserId);
        return ResponseEntity.created(location).build();
    }

    /**
     * 2) 로그인
     * POST /user/login
     */
    @PostMapping("/login")
    @Transactional
    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인하고 JWT 토큰을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "로그인 실패")
    })
    public ResponseEntity<JwtTokenResponse> login(@RequestBody LoginRequest request) {
        log.info("Request to login user: {}", request.getEmail());
        JwtTokenResponse tokens = userService.login(request);
        return ResponseEntity.ok(tokens);
    }

    /**
     * 3) 로그아웃
     * DELETE /user/logout
     */
    @DeleteMapping("/logout")
    @Transactional
    @Operation(summary = "로그아웃", description = "현재 사용자의 토큰을 무효화하고 세션을 종료합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.startsWith("Bearer ")
            ? authorization.substring(7)
            : authorization;
        log.info("Request to logout token: {}", token);
        userService.logout(token);
        return ResponseEntity.noContent().build();
    }

    /**
     * 4) 내 정보 조회
     * GET /user/me
     */
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal User principal) {
        log.info("Request to get current user: {}", principal.getId());
        User user = userService.findById(principal.getId());
        return ResponseEntity.ok(user);
    }

    /**
     * 5) 내 정보 수정
     * PUT /user/me
     */
    @PutMapping("/me")
    @Transactional
    @Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content),
        @ApiResponse(responseCode = "400", description = "입력 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<User> updateCurrentUser(
        @AuthenticationPrincipal User principal,
        @RequestBody UpdateUserRequest request
    ) {
        log.info("Request to update current user {}: email={}, nickname={} ",
            principal.getId(), request.getEmail(), request.getNickname());
        User updated = userService.updateUser(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 6) 회원 탈퇴
     * DELETE /user/me
     */
    @DeleteMapping("/me")
    @Transactional
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자를 탈퇴 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal User principal) {
        log.info("Request to delete current user: {}", principal.getId());
        userService.deleteUser(principal.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 7) 관리자: 특정 사용자 조회
     * GET /user/admin/{userId}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{userId}")
    public ResponseEntity<User> getAnyUser(@PathVariable Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * 8) 관리자: 특정 사용자 수정
     * PUT /user/admin/{userId}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{userId}")
    @Transactional
    public ResponseEntity<User> updateAnyUser(
        @PathVariable Long userId,
        @RequestBody UpdateUserRequest request
    ) {
        User updated = userService.updateUser(userId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 9) 관리자: 특정 사용자 삭제
     * DELETE /user/admin/{userId}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{userId}")
    @Transactional
    public ResponseEntity<Void> deleteAnyUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}