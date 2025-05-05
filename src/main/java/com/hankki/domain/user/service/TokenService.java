package com.hankki.domain.user.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.hankki.config.jwt.TokenProvider;
import com.hankki.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TokenService {
	private final TokenProvider tokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final UserService userService;
	
	public String createNewAccessToken(String refreshToken) {
		if (!tokenProvider.validToken(refreshToken)) {
			throw new IllegalArgumentException("Unexpected token");
		}
		Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
		User user = userService.findById(userId);
		return tokenProvider.generateToken(user, Duration.ofHours(2));
	}
}
