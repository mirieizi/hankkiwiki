package com.hankki.domain.auth.service;

import org.springframework.stereotype.Service;

import com.hankki.domain.auth.dto.RefreshToken;
import com.hankki.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
	private final RefreshTokenRepository refreshTokenRepository;
	public RefreshToken findByRefreshToken(String refreshToken) {
		return refreshTokenRepository.findByRefreshToken(refreshToken)
				.orElseThrow(() -> new IllegalArgumentException("Unexpected token"));
	}
}
