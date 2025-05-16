package com.hankki.domain.auth.service;

import org.springframework.stereotype.Service;
import com.hankki.common.jwt.TokenProvider;
import com.hankki.domain.auth.entity.RefreshToken;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TokenService {
	private final TokenProvider tokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final UserService userService;
	// 기존에는 형식만 체크, 이제는 만료까지 체크함.
	public String createNewAccessToken(String refreshToken) {
		if (!tokenProvider.validateRefreshToken(refreshToken)) { // 토큰 형식 및 만료 여부 체크
			throw new IllegalArgumentException("만료된 리프레시토큰입니다.");
		}
		RefreshToken tokenEntity = refreshTokenService.findByRefreshToken(refreshToken); // 2. 리프레시 토큰 조회
		User user = userService.findById(tokenEntity.getUserId()); //3. 사용자 정보 조회
		return tokenProvider.generateAccessToken(user); // 4. 새로운 엑세스 토큰 발급
	}
}
