package com.hankki.domain.user.controller;

import com.hankki.domain.user.dto.UserResponse;
import com.hankki.domain.user.service.UserHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hankki.common.security.principal.CurrentUser;
import com.hankki.domain.auth.dto.UserPrincipal;
import com.hankki.domain.user.dto.UpdateUserRequest;
import com.hankki.domain.user.dto.UserProfileResponse;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 사용자 계정 관리 컨트롤러
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {
    private final UserService userService;
    private final UserHealthService userHealthService;

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
    public ResponseEntity<UserResponse> getCurrentUser(@CurrentUser UserPrincipal principal) {
        log.info("Request to get current user: {}", principal.getUserId());
        return ResponseEntity.ok(UserResponse.builder()
                .id(principal.getUserId())
                .email(principal.getEmail())
                .nickname(principal.getNickname())
                .build());
    }

    /**
     * 5) 내 정보 수정
     * PUT /user/me
     */
    @PatchMapping("/me")
    @Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content),
        @ApiResponse(responseCode = "400", description = "입력 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<UserResponse> updateCurrentUser(
        @CurrentUser UserPrincipal principal,
        @RequestBody UpdateUserRequest request
    ) {
        log.info("Request to update current user {}: email={}, nickname={} ",
            principal.getUserId(), request.getEmail(), request.getNickname());
        User updated = userService.updateUser(principal.getUserId(), request);
        return ResponseEntity.ok(UserResponse.builder()
                .id(updated.getId())
                .email(updated.getEmail())
                .nickname(updated.getNickname())
                .build());
    }

    /**
     * 6) 회원 탈퇴
     * DELETE /user/me
     */
    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자를 탈퇴 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<String> deleteCurrentUser(@CurrentUser UserPrincipal principal) {
        log.info("Request to delete current user: {}", principal.getUserId());
        userService.deleteUser(principal.getUserId());
        return ResponseEntity.ok("사용자 탈퇴 처리를 완료했습니다.");
    }

    /**
     * 7) 관리자: 특정 사용자 조회
     * GET /user/admin/{userId}
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/{userId}")
    public ResponseEntity<UserResponse> getAnyUser(@PathVariable Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build());
    }

    /**
     * 8) 관리자: 전체 사용자 조회
     * GET /user/admin
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAllUsers()
                .stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                .build())
                .toList();
        return ResponseEntity.ok(users);
    }

	/**
	 * 9) 관리자: 특정 사용자 삭제 DELETE /user/admin/{userId}
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@DeleteMapping("/admin/{userId}")
	public ResponseEntity<String> deleteAnyUser(@PathVariable Long userId) {
		userService.deleteUser(userId);
		return ResponseEntity.ok("관리자 기능으로 사용자 삭제 완료했습니다.");
	}

	@GetMapping("/profile")
	public ResponseEntity<UserProfileResponse> getProfile(@CurrentUser UserPrincipal principal) {
		UserProfileResponse profile = userService.getProfile(principal.getUserId());
		return ResponseEntity.ok(profile);
	}

}
