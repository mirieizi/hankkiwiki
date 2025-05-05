package com.hankki.config.jwt;

import java.time.Duration;
import java.util.Date;
import java.util.Set;
import java.util.Collections;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.hankki.domain.user.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TokenProvider {
	private final JwtProperties jwtProperties;
	public String generateToken(User user, Duration expiredAt) {
		Date now = new Date();
		return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
	}
	
	public String makeToken(Date expiry, User user) {
		Date now = new Date();
		return Jwts.builder()
				.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
				.setIssuer(jwtProperties.getIssuer())
				.setIssuedAt(now)
				.setExpiration(expiry)
				.setSubject(user.getEmail())
				.claim("id", user.getId())
				.signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
				.compact();
	}
	public boolean validToken(String token) {
		try {
			Jwts.parser()
			.setSigningKey(jwtProperties.getSecretKey())
			.parseClaimsJws(token);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	public Authentication getAuthentication(String token) {
		Claims claims = getClaims(token);
		Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
		return new UsernamePasswordAuthenticationToken(
			    claims.getSubject(),    // principal (이메일)
			    null,                   // credentials (패스워드 or null)
			    authorities             // GrantedAuthority 컬렉션
			);
		
	}
	// 토큰 기반으로 유저 ID를 가져오는 메서드
	public Long getUserId(String token) {
		Claims claims = getClaims(token);
		return claims.get("id", Long.class);
	}
	private Claims getClaims(String token) {
		return Jwts.parser()
				.setSigningKey(jwtProperties.getSecretKey())
				.parseClaimsJws(token)
				.getBody();
	}
}
