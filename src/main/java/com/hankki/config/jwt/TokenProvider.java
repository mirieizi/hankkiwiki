
package com.hankki.config.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.service.UserDetailService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * JWT 토큰 생성 및 검증 서비스
 */
@RequiredArgsConstructor
@Service
public class TokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private final JwtProperties     jwtProperties;
    private final UserDetailService userDetailService;   // [피드백 반영] UserDetailsService 주입

    // 내부 토큰 구분용 enum (Access vs Refresh)  [피드백 반영]
    private enum TokenType { ACCESS, REFRESH }

    // === 토큰 생성 메서드 분리 ===

    /**
     * AccessToken 생성
     */
    public String generateAccessToken(User user) {
        // [피드백 반영] 권한 및 타입 claim 추가
        return generateToken(user, jwtProperties.getAccessTokenValidity(), TokenType.ACCESS);
    }

    /**
     * RefreshToken 생성
     */
    public String generateRefreshToken(User user) {
        // [피드백 반영] RefreshToken 별도 생성 지원
        return generateToken(user, jwtProperties.getRefreshTokenValidity(), TokenType.REFRESH);
    }

    /**
     * 공통 토큰 생성 로직
     */
    private String generateToken(User user, Duration validity, TokenType type) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + validity.toMillis());
        SecretKey key = Keys.hmacShaKeyFor(
            jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8)
        );  // [보안] SecretKey는 서버 내에만 보관

        List<String> roles = Collections.singletonList("ROLE_USER");  // [피드백 반영] roles claim 추가

        return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setIssuer(jwtProperties.getIssuer())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .setSubject(user.getEmail())
            .claim("id", user.getId())        // [피드백 반영] 사용자 ID claim
            .claim("type", type.name())       // [피드백 반영] 토큰 타입 claim
            .claim("roles", roles)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    // === 토큰 검증 메서드 분리 ===

    /**
     * AccessToken 유효성 검사
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token, TokenType.ACCESS.name());
    }

    /**
     * RefreshToken 유효성 검사
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token, TokenType.REFRESH.name());
    }

    /**
     * 공통 검증 로직: 타입 일치 여부 확인 및 예외 처리  [피드백 반영]
     */
    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            String actualType = claims.get("type", String.class);
            if (!expectedType.equals(actualType)) {
                logger.warn("Unexpected token type: {} (expected {})", actualType, expectedType);
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    // === 인증 정보 조회 ===

    /**
     * AccessToken 기반 인증 정보 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String type   = claims.get("type", String.class);
        if (!TokenType.ACCESS.name().equals(type)) {
            // [피드백 반영] AccessToken 이 아닐 시 예외
            throw new JwtException("Attempt to use non-access token for authentication");
        }

        String email = claims.getSubject();
        UserDetails userDetails = userDetailService.loadUserByUsername(email);  // [피드백 반영] 활성화 상태 등 체크 가능
        return new UsernamePasswordAuthenticationToken(
            userDetails, token, userDetails.getAuthorities()
        );
    }

    // === 편의 메서드 ===

    /**
     * 토큰에서 사용자 ID 조회
     */
    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("id", Long.class);
    }

    /**
     * Claims 파싱 로직
     */
    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
            jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8)
        );
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
