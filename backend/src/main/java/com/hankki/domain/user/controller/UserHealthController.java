package com.hankki.domain.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.hankki.domain.user.dto.DailyCalorieResponse;
import com.hankki.domain.user.dto.UpdateUserHealthRequest;
import com.hankki.domain.user.dto.UserHealthInfoResponse;
import com.hankki.domain.user.dto.UserHealthRequest;
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
	 * /** 1) 일반 사용자: 자신의 건강정보 등록 POST /user/me/health
	 */
	@PostMapping("/me/health")
	@Transactional
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<Long> registerOwnHealth(@CurrentUser UserPrincipal principal,
			@Validated @RequestBody UserHealthRequest request) {
		Long userId = principal.getUserId();
		log.info("Register health for self, userId={}", userId);
		Long savedId = userHealthService.registerHealthInfo(userId, request);
		return ResponseEntity.ok(savedId);
	}

	/**
	 * /** 2) 관리자: 모든 사용자의 건강정보 등록 POST /user/admin/health/{userId}
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/admin/health/{userId}")
	@Transactional
	public ResponseEntity<Long> registerAnyHealth(@PathVariable Long userId,
			@Validated @RequestBody UserHealthRequest request) {
		log.info("Admin register health for userId={}", userId);
		Long savedId = userHealthService.registerHealthInfo(userId, request);
		return ResponseEntity.ok(savedId);
	}

	/**
	 * /** 3) 조회 GET /user/me/health - 자신의 건강정보 조회 GET /user/admin/health/{userId} -
	 * 관리자가 사용자의 건강정보 조회
	 */
	@GetMapping("/me/health")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<UserHealthInfoResponse> getOwnHealth(@CurrentUser UserPrincipal principal) {
		Long userId = principal.getUserId();
		UserHealthInfoResponse info = userHealthService.findHealthById(userId);
		return ResponseEntity.ok(info);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/health/{userId}")
	public ResponseEntity<UserHealthInfoResponse> getAnyHealth(@PathVariable Long userId) {
		UserHealthInfoResponse info = userHealthService.findHealthById(userId);
		return ResponseEntity.ok(info);
	}

	/**
	 * /** 4) 수정 PUT /user/me/health - 자신의 건강정보 수정 PUT /user/admin/health/{userId} -
	 * 관리자가 사용자의 건강정보 수정
	 */
	@PatchMapping("/me/health")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<UserHealthInfo> updateOwnHealth(@CurrentUser UserPrincipal principal,
			@Validated @RequestBody UpdateUserHealthRequest request) {
		Long userId = principal.getUserId();
		UserHealthInfo updated = userHealthService.updateHealthInfo(userId, request);
		return ResponseEntity.ok(updated);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/admin/health/{userId}")
	public ResponseEntity<UserHealthInfo> updateAnyHealth(@PathVariable Long userId,
			@Validated @RequestBody UpdateUserHealthRequest request) {
		UserHealthInfo updated = userHealthService.updateHealthInfo(userId, request);
		return ResponseEntity.ok(updated);
	}

	/**
	 * /** 5) 삭제 DELETE /user/me/health - 자신의 건강정보 삭제 DELETE
	 * /user/admin/health/{userId} - 관리자가 사용자의 건강정보 삭제
	 */
	@DeleteMapping("/me/health")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<Void> deleteOwnHealth(@CurrentUser UserPrincipal principal) {
		Long userId = principal.getUserId();
		userHealthService.deleteHealthInfo(userId);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/admin/health/{userId}")
	public ResponseEntity<Void> deleteAnyHealth(@PathVariable Long userId) {
		userHealthService.deleteHealthInfo(userId);
		return ResponseEntity.noContent().build();
	}
	
	@PreAuthorize("hasRole('User')")
	@GetMapping("/me/health/dailycalorie")
	public ResponseEntity<DailyCalorieResponse> getUserDailyCalorie(@CurrentUser UserPrincipal principal){
		Long userId = principal.getUserId();
		DailyCalorieResponse calorie =  userHealthService.getUserDailyCalorie(userId);
		return ResponseEntity.ok(calorie);
	}
	
}
