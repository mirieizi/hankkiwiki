package com.hankki.domain.user.service;

import com.hankki.domain.user.dto.DailyCalorieResponse;
import com.hankki.domain.user.dto.UpdateUserHealthRequest;
import com.hankki.domain.user.dto.UpdateUserResponse;
import com.hankki.domain.user.dto.UserHealthInfoResponse;
import com.hankki.domain.user.dto.UserHealthRequest;
import com.hankki.domain.user.entity.UserHealthInfo;

/**
 * 사용자 건강정보 로직을 담당하는 서비스
 */
public interface UserHealthService {
	/** 사용자 건강정보 등록 처리	 
	 * @param userId */
	Long registerHealthInfo(Long userId, UserHealthRequest request);
	
	/** ID로 사용자 건강정보 조회	 */
	UserHealthInfoResponse findHealthById(Long userId);
	
	/** 사용자 건강정보 수정 */
	UserHealthInfo updateHealthInfo(Long userId, UpdateUserHealthRequest request);
	
	/** 사용자 건강 정보 삭제 */
	void deleteHealthInfo(Long userId);

	/** 사용자 일일칼로리 소모량 조회 */
	DailyCalorieResponse getUserDailyCalorie(Long userId);
}
