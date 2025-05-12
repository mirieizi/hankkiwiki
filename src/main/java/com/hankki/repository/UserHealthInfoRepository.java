package com.hankki.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hankki.domain.user.entity.UserHealthInfo;

public interface UserHealthInfoRepository extends JpaRepository<UserHealthInfo, Long> {
	Optional<UserHealthInfo> findByUserId(Long userID);
}
