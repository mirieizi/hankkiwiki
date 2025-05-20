package com.hankki.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.hankki.domain.auth.entity.RefreshToken;

import jakarta.transaction.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
	Optional<RefreshToken> findByUserId(Long userId);
	Optional<RefreshToken> findByRefreshToken(String refreshToken);
	@Modifying
	@Transactional
	void deleteByRefreshToken(String refreshToken);
	@Modifying
	@Transactional
	void deleteByUserId(Long userId);

}
