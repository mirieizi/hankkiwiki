package com.hankki.domain.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.common.security.jwt.TokenProvider;
import com.hankki.domain.auth.dto.request.LoginRequest;
import com.hankki.domain.auth.dto.request.SignUpRequest;
import com.hankki.domain.auth.dto.response.JwtTokenResponse;
import com.hankki.domain.auth.entity.AuthUser;
import com.hankki.domain.auth.entity.RefreshToken;
import com.hankki.domain.auth.repository.RefreshTokenRepository;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    
    /**
     * 회원가입
     */
    @Transactional
    public Long signUp(SignUpRequest request) {
        log.info("Request to sign up user: {}", request.getEmail());
        
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email already exists: {}", request.getEmail());
            throw new HankkiWikiException(ExceptionStatus.EMAIL_DUPLICATION);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.getNickname())) {
            log.error("Nickname already exists: {}", request.getNickname());
            throw new HankkiWikiException(ExceptionStatus.NICKNAME_DUPLICATION);
        }

        // 사용자 생성
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .build();

        Long id = userRepository.save(user).getId();
        log.debug("User signed up with ID: {}", id);
        return id;
    }

    /**
     * 로그인
     */
    @Transactional
    public JwtTokenResponse login(LoginRequest request) {
        // 1) 인증
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2) 토큰 생성
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        User user = authUser.getUser();
        String accessToken  = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        // 3) 기존 토큰 조회 → 있으면 update, 없으면 insert
        refreshTokenRepository.findByUserId(user.getId())
            .map(existing -> existing.update(refreshToken))
            .orElseGet(() -> refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken)));

        return new JwtTokenResponse(accessToken, refreshToken);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Long userId) {
        log.info("Request to logout for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.error("User not found for ID: {}", userId);
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        refreshTokenRepository.deleteByUserId(userId);
        log.debug("All refresh tokens invalidated for user ID: {}", userId);
    }
}
