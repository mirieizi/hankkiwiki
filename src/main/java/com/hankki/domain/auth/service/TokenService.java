package com.hankki.domain.auth.service;

import org.springframework.stereotype.Service;
import com.hankki.common.jwt.TokenProvider;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TokenService {
	private final TokenProvider tokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final UserService userService;
	
	public String createNewAccessToken(String refreshToken) {
		if (!tokenProvider.validateRefreshToken(refreshToken)) {
			throw new IllegalArgumentException("Unexpected token");
		}
		Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
		User user = userService.findById(userId);
		return tokenProvider.generateAccessToken(user);
	}
}
