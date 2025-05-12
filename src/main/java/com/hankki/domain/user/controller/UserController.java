package com.hankki.domain.user.controller;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hankki.domain.auth.dto.JwtTokenResponse;
import com.hankki.domain.auth.dto.LoginRequest;
import com.hankki.domain.auth.dto.SignUpRequest;
import com.hankki.domain.user.dto.UpdateUserRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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

    @PostMapping("/logout")
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
        log.info("Request to update current user {}: email={}, nickname={}",
            principal.getId(), request.getEmail(), request.getNickname());
        User updated = userService.updateUser(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

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
}
