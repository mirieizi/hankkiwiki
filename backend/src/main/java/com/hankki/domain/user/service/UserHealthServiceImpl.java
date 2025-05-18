package com.hankki.domain.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hankki.domain.user.dto.UpdateUserHealthRequest;
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
public class UserHealthServiceImpl implements UserHealthService {
    private static final Logger log = LoggerFactory.getLogger(UserHealthServiceImpl.class);

    private final UserHealthInfoRepository healthRepo;
    private final UserRepository userRepo;

    /**
     * 사용자 건강정보 등록
     * 관리자: 모든 사용자 등록 가능
     * 일반 사용자: 자기 자신만 등록 가능
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public Long registerHealthInfo(Long userId, UserHealthRequest request) {
        log.info("Request to register HealthInfo for userId={}", userId);

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID: " + userId));

        UserHealthInfo info = UserHealthInfo.builder()
            .user(user)
            .gender(request.getGender())
            .height(request.getHeight())
            .weight(request.getWeight())
            .age(request.getAge())
            .activityFactor(request.getActivityFactor())
            .build();  // recommendedCalorie는 엔티티 콜백으로 자동 계산

        return healthRepo.save(info).getId();
    }

    /**
     * 사용자 건강정보 조회
     * 관리자: 모든 사용자 조회 가능
     * 일반 사용자: 자기 자신만 조회 가능
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserHealthInfo findHealthById(Long userId) {
        return healthRepo.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자 ID의 건강정보가 없습니다: " + userId));
    }

    /**
     * 사용자 건강정보 수정
     * 관리자: 모든 사용자 수정 가능
     * 일반 사용자: 자기 자신만 수정 가능
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserHealthInfo updateHealthInfo(Long userId, UpdateUserHealthRequest request) {
        UserHealthInfo info = healthRepo.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자 ID의 건강정보가 없습니다: " + userId));

        // 변경 메서드 호출
        info.changeHeight(request.getHeight());
        info.changeWeight(request.getWeight());
        info.changeAge(request.getAge());
        info.changeGender(request.getGender());
        info.changeActivityFactor(request.getActivityFactor());

        // save 호출 없이 @Transactional에서 자동 변경 감지 (dirty checking)
        return info;
    }

    /**
     * 사용자 건강정보 삭제
     * 관리자: 모든 사용자 삭제 가능
     * 일반 사용자: 자기 자신만 삭제 가능
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public void deleteHealthInfo(Long userId) {
        UserHealthInfo info = healthRepo.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자 ID의 건강정보가 없습니다: " + userId));
        healthRepo.delete(info);
    }


}
