package com.hankki.domain.auth.service;

import org.springframework.stereotype.Service;

import com.hankki.domain.auth.entity.RefreshToken;
import com.hankki.domain.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
	private final RefreshTokenRepository refreshTokenRepository;
	public RefreshToken findByRefreshToken(String refreshToken) {
		return refreshTokenRepository.findByRefreshToken(refreshToken)
				.orElseThrow(() -> new IllegalArgumentException("리프레시 토큰을 찾을 수 없습니다."));
	}
	
	public void deleteByRefreshToken(String refreshToken) {
		refreshTokenRepository.deleteByRefreshToken(refreshToken);
	}
}
