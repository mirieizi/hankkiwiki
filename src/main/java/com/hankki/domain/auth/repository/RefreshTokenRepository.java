package com.hankki.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hankki.domain.auth.dto.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
	Optional<RefreshToken> findByUserId(Long userId);
	Optional<RefreshToken> findByRefreshToken(String refreshToken);

}
