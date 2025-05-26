package com.hankki.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.user.dto.DailyCalorieResponse;
import com.hankki.domain.user.dto.UpdateUserHealthRequest;
import com.hankki.domain.user.dto.UserHealthInfoResponse;
import com.hankki.domain.user.dto.UserHealthRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.entity.UserHealthInfo;
import com.hankki.domain.user.repository.UserHealthInfoRepository;
import com.hankki.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 건강정보의 CRUD를 관리하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserHealthServiceImpl implements UserHealthService {

	private final UserHealthInfoRepository healthRepo;
	private final UserRepository userRepo;

	/**
	 * 사용자 건강정보 등록 관리자: 모든 사용자 등록 가능 일반 사용자: 자기 자신만 등록 가능
	 */
	@Override
	@Transactional
	public Long registerHealthInfo(Long userId, UserHealthRequest request) {
		log.info("Request to register HealthInfo for userId={}", userId);

		User user = userRepo.findById(userId)
				.orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER));

		UserHealthInfo info = UserHealthInfo.builder()
				.gender(request.getGender())
				.height(request.getHeight())
				.weight(request.getWeight())
				.age(request.getAge())
				.activityFactor(request.getActivityFactor())
				.userId(userId)
				.build();

		return healthRepo.save(info).getId();
	}

	/**
	 * 사용자 건강정보 조회 관리자: 모든 사용자 조회 가능 일반 사용자: 자기 자신만 조회 가능
	 */
	@Override
	@Transactional(readOnly = true)
	public UserHealthInfoResponse findHealthById(Long userId) {
		UserHealthInfo info = healthRepo.findByUserId(userId)
				.orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER_HEALTH));
		return UserHealthInfoResponse.from(info);
	}

	/**
	 * 사용자 건강정보 수정 관리자: 모든 사용자 수정 가능 일반 사용자: 자기 자신만 수정 가능
	 */
	@Override
	@Transactional
	public UserHealthInfo updateHealthInfo(Long userId, UpdateUserHealthRequest request) {
		UserHealthInfo info = healthRepo.findByUserId(userId)
				.orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER));

		// 변경 메서드 호출
		info.changeHeight(request.getHeight());
		info.changeWeight(request.getWeight());
		info.changeAge(request.getAge());
		info.changeGender(request.getGender());
		info.changeActivityFactor(request.getActivityFactor());

		// save 호출 없이 @Transactional에서 자동 변경 감지 (dirty checking)
		return info;
	}

	@Override
	@Transactional
	public void deleteHealthInfo(Long userId) {
		// 1) 사용자 존재 여부 확인
		userRepo.findById(userId).orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER));


		// 2) 건강정보 조회
		UserHealthInfo info = healthRepo.findByUserId(userId)
				.orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER_HEALTH));

		// 3) 바로 삭제
		healthRepo.delete(info);
	}

	@Override
	@Transactional(readOnly = true)
	public DailyCalorieResponse getUserDailyCalorie(Long userId) {
		// 1) 사용자 존재 여부 확인
		userRepo.findById(userId).orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER));

		// 2) 칼로리 조회
		return healthRepo.findDailyCalorieByUserId(userId)
				.orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER_HEALTH));
	}
}
