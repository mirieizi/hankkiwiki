package com.hankki.domain.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.hankki.config.jwt.TokenProvider;
import com.hankki.domain.auth.dto.JwtTokenResponse;
import com.hankki.domain.auth.dto.LoginRequest;
import com.hankki.domain.auth.dto.SignUpRequest;
import com.hankki.domain.user.dto.UpdateUserRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 회원가입, 로그인, 조회, 수정, 삭제 로직을 담당하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @Override
    @Transactional
    public Long signUp(SignUpRequest request) {
        log.info("Request to sign up user: {}", request.getEmail());
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .build();
        Long id = userRepository.save(user).getId();
        log.debug("User signed up with ID: {}", id);
        return id;
    }

    @Override
    @Transactional
    public JwtTokenResponse login(LoginRequest request) {
        log.info("Request to login user: {}", request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        log.debug("User {} logged in, tokens generated", user.getEmail());
        return new JwtTokenResponse(accessToken, refreshToken);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Transactional
    public User findById(Long userId) {
        log.info("Request to find user by ID: {}", userId);
        return userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with ID: {}", userId);
                return new IllegalArgumentException("User not found: " + userId);
            });
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request) {
        log.info("Request to update user ID {}: newEmail={}, newNickname={}",
                 userId, request.getEmail(), request.getNickname());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("Cannot update, user not found ID: {}", userId);
                return new IllegalArgumentException("User not found: " + userId);
            });

        // 이메일 변경
        if (request.getEmail() != null) {
            user.changeEmail(request.getEmail());
            log.debug("Email updated for user ID: {}", userId);
        }
        // 비밀번호 변경
        if (request.getPassword() != null) {
            user.changePassword(request.getPassword(), passwordEncoder);
            log.debug("Password updated for user ID: {}", userId);
        }
        // 닉네임 변경
        if (request.getNickname() != null) {
            user.changeNickname(request.getNickname());
            log.debug("Nickname updated for user ID: {}", userId);
        }

        return user;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Request to delete user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.error("Cannot delete, user not found ID: {}", userId);
            throw new IllegalArgumentException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
        log.debug("Deleted user with ID: {}", userId);
    }
    @Override
    @Transactional
    public void logout(String token) {
    	log.info("Request to logout token: {}", token);
        tokenProvider.invalidateToken(token);
    	
    }

}
