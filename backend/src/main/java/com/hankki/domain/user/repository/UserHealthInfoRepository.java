package com.hankki.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hankki.domain.user.dto.DailyCalorieResponse;
import com.hankki.domain.user.entity.UserHealthInfo;

import jakarta.transaction.Transactional;

public interface UserHealthInfoRepository extends JpaRepository<UserHealthInfo, Long> {
    
    // 1) userId 로 전체 UserHealthInfo 조회
    Optional<UserHealthInfo> findByUserId(Long userId);

    // 2) projection 으로 칼로리만 뽑아오기
    @Query("""
      SELECT new com.hankki.domain.user.dto.DailyCalorieResponse(u.recommendedCalorie)
        FROM UserHealthInfo u
       WHERE u.userId = :userId
    """)
    Optional<DailyCalorieResponse> findDailyCalorieByUserId(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserHealthInfo u WHERE u.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
