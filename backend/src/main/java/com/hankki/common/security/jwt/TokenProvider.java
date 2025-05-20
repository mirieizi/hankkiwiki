
package com.hankki.common.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.hankki.domain.user.entity.User;

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
    private final UserDetailsService userDetailsService;   // [피드백 반영] UserDetailsService 주입

    /**
     * 무효화된 토큰을 관리하는 블랙리스트
     * 이거는 인 메모리 타입으로 서버 꺼졌다가 다시 켜지면 무효화 됨 -> 임시로..
     */
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    
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
            .claim("id", user.getId())
            .claim("type", type.name())       // 토큰 타입 claim
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
     * 공통 검증 로직:
     *  - 토큰 형식, 서명 유효성, 타입 일치, 블랙리스트 확인
     */
    private boolean validateToken(String token, String expectedType) {
        if (blacklist.contains(token)) {
            logger.warn("Token is blacklisted: {}", token);
            return false;
        }
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
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);  // [피드백 반영] 활성화 상태 등 체크 가능
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
    // 단순히 종료 시키면 JWT 인증 시스템에서는 서버는 유효한 토큰인지만 확인함
    // -> 세션 관리는 안 하게 되지만 만약 토큰이 탈취된 경우, 토큰 만료 전까지 탈취자가 토큰 사용하여 시스템 접속 가능
    // -> 블랙 리스트 사용함! (다만 이렇게 하면 약간 서버 부하 증가 (모든 토큰 DB 비교해야 함)
    // 제대로 하려면 redis 사용해야 함
    /**
     * 토큰 무효화 (로그아웃 등)
     */
    public void invalidateToken(String token) {
        blacklist.add(token);
        logger.info("Token invalidated: {}", token);
    }
}
