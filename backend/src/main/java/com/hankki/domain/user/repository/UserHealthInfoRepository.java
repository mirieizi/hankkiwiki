package com.hankki.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hankki.domain.user.dto.DailyCalorieResponse;
import com.hankki.domain.user.entity.UserHealthInfo;

public interface UserHealthInfoRepository extends JpaRepository<UserHealthInfo, Long> {
	@EntityGraph(attributePaths = {"user"})
	Optional<UserHealthInfo> findByUserId(Long userID);
	@Query("SELECT new com.hankki.domain.user.dto.DailyCalorieResponse(uhi.recommendedCalorie) " +
		       "FROM UserHealthInfo uhi " +
		       "WHERE uhi.user.id = :userId")
		Optional<DailyCalorieResponse> findDailyCalorie(@Param("userId") Long userId);

}
