package com.hankki.domain.user.controller;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hankki.common.security.principal.CurrentUser;
import com.hankki.domain.auth.dto.UserPrincipal;
import com.hankki.domain.auth.dto.request.LoginRequest;
import com.hankki.domain.auth.dto.request.SignUpRequest;
import com.hankki.domain.auth.dto.response.JwtTokenResponse;
import com.hankki.domain.user.dto.UpdateUserResponse;
import com.hankki.domain.user.dto.UserProfileResponse;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 계정 관리 컨트롤러
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	private final UserService userService;

//    /**
//     * 1) 회원 가입
//     * POST /user/signup
//     */
//    @PostMapping("/signup")
//    @Operation(summary = "회원 가입", description = "이메일·비밀번호·닉네임으로 신규 회원을 등록합니다.")
//    @ApiResponses({
//        @ApiResponse(responseCode = "201", description = "회원 생성 성공", content = @Content),
//        @ApiResponse(responseCode = "400", description = "회원 생성 실패")
//    })
//    public ResponseEntity<Void> signup(@RequestBody SignUpRequest request) {
//        log.info("Request to sign up user: {}", request.getEmail());
//        Long newUserId = userService.signUp(request);
//        URI location = URI.create("/user/" + newUserId);
//        return ResponseEntity.created(location).build();
//    }
//
//    /**
//     * 2) 로그인
//     * POST /user/login
//     */
//    @PostMapping("/login")
//    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인하고 JWT 토큰을 반환합니다.")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content),
//        @ApiResponse(responseCode = "401", description = "로그인 실패")
//    })
//    public ResponseEntity<JwtTokenResponse> login(@RequestBody LoginRequest request) {
//        log.info("Request to login user: {}", request.getEmail());
//        JwtTokenResponse tokens = userService.login(request);
//        return ResponseEntity.ok(tokens);
//    }
//
//    /**
//     * 3) 로그아웃
//     * DELETE /user/logout
//     */
//    @DeleteMapping("/logout")
//    @Operation(summary = "로그아웃", description = "현재 사용자의 토큰을 무효화하고 세션을 종료합니다.")
//    @ApiResponses({
//        @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
//        @ApiResponse(responseCode = "401", description = "인증 실패")
//    })
//    public ResponseEntity<Void> logout(@CurrentUser UserPrincipal principal) {
//        log.info("Request to logout token: {}", principal.getEmail());
//        userService.logout(principal.getUserId());
//        return ResponseEntity.noContent().build();
//    }

	/**
	 * 4) 내 정보 조회 GET /user/me
	 */
	@GetMapping("/me")
	@Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 반환합니다.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content),
			@ApiResponse(responseCode = "401", description = "인증 실패") })
	public ResponseEntity<UserPrincipal> getCurrentUser(@CurrentUser UserPrincipal principal) {
		log.info("Request to get current user: {}", principal.getUserId());
		return ResponseEntity.ok(principal);
	}

	/**
	 * 5) 내 정보 수정 PUT /user/me
	 */
	@PatchMapping("/me")
	@Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content),
			@ApiResponse(responseCode = "400", description = "입력 값 오류"),
			@ApiResponse(responseCode = "401", description = "인증 실패") })
	public ResponseEntity<UserPrincipal> updateCurrentUser(@CurrentUser UserPrincipal principal,
			@RequestBody UpdateUserResponse request) {
		log.info("Request to update current user {}: email={}, nickname={} ", principal.getUserId(), request.getEmail(),
				request.getNickname());
		User updated = userService.updateUser(principal.getUserId(), request);
		return ResponseEntity.ok(UserPrincipal.from(updated));
	}

	/**
	 * 6) 회원 탈퇴 DELETE /user/me
	 */
	@DeleteMapping("/me")
	@Operation(summary = "회원 탈퇴", description = "로그인한 사용자를 탈퇴 처리합니다.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
			@ApiResponse(responseCode = "401", description = "인증 실패") })
	public ResponseEntity<Void> deleteCurrentUser(@CurrentUser UserPrincipal principal) {
		log.info("Request to delete current user: {}", principal.getUserId());
		userService.deleteUser(principal.getUserId());
		return ResponseEntity.noContent().build();
	}

	/**
	 * 7) 관리자: 특정 사용자 조회 GET /user/admin/{userId}
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/admin/{userId}")
	public ResponseEntity<UserPrincipal> getAnyUser(@PathVariable Long userId) {
		User user = userService.findById(userId);
		return ResponseEntity.ok(UserPrincipal.from(user));
	}

	/**
	 * 8) 관리자: 전체 사용자 조회 GET /user/admin
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/admin")
	@Transactional
	public ResponseEntity<List<UserPrincipal>> getAllUsers() {
		List<UserPrincipal> users = userService.findAllUsers().stream().map(UserPrincipal::from).toList();
		return ResponseEntity.ok(users);
	}

	/**
	 * 9) 관리자: 특정 사용자 삭제 DELETE /user/admin/{userId}
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@DeleteMapping("/admin/{userId}")
	@Transactional
	public ResponseEntity<Void> deleteAnyUser(@PathVariable Long userId) {
		userService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/profile")
	public ResponseEntity<UserProfileResponse> getProfile(@CurrentUser UserPrincipal principal) {
		UserProfileResponse profile = userService.getProfile(principal.getUserId());
		return ResponseEntity.ok(profile);
	}

}
