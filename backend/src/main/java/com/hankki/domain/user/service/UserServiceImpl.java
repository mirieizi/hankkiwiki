package com.hankki.domain.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hankki.common.jwt.TokenProvider;
import com.hankki.domain.auth.dto.request.LoginRequest;
import com.hankki.domain.auth.dto.request.SignUpRequest;
import com.hankki.domain.auth.dto.response.JwtTokenResponse;
import com.hankki.domain.auth.entity.RefreshToken;
import com.hankki.domain.auth.repository.RefreshTokenRepository;
import com.hankki.domain.user.dto.UpdateUserRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 사용자 회원가입, 로그인, 조회, 수정, 삭제 로직을 담당하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    
    /**
     * 회원가입
     * @param SignUpRequest (이메일, 비밀번호, 닉네임)
     * @return id (userId)
     */
    @Override
    @Transactional
    public Long signUp(SignUpRequest request) {
        log.info("Request to sign up user: {}", request.getEmail());
        
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email already exists: {}", request.getEmail());
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.getNickname())) {
            log.error("Nickname already exists: {}", request.getNickname());
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
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
     * JwtToken을 사용한 로그인
     * @param LoginRequest 
     * @return JwtToken
     */
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

        // RefreshToken 저장
        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken));
        log.debug("User {} logged in, tokens generated", user.getEmail());

        return new JwtTokenResponse(accessToken, refreshToken);
    }
    
    /**
     * userId로 user를 반환
     * @param userId
     * @return User
     */
    @Override
    @Transactional
    public User findById(Long userId) {
        log.info("Request to find user by ID: {}", userId);
        return userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with ID: {}", userId);
                return new IllegalArgumentException("User not found: " + userId);
            });
    }

    /**
     * 사용자 정보 수정
     * @param userId
     * @param UpdateUserRequest
     * @return User
     */
    @Override
    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request) {
        log.info("Request to update user ID {}: newEmail={}, newNickname={}, newPassword={}",
                 userId, request.getEmail(), request.getNickname(), request.getPassword());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("Cannot update, user not found ID: {}", userId);
                return new IllegalArgumentException("User not found: " + userId);
            });

        // 이메일 변경
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                log.error("Email already in use: {}", request.getEmail());
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            user.changeEmail(request.getEmail());
            log.debug("Email updated for user ID: {}", userId);
        }

        // 비밀번호 변경 (암호화)
        if (request.getPassword() != null && !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.changePassword(passwordEncoder.encode(request.getPassword()), passwordEncoder);
            log.debug("Password updated for user ID: {}", userId);
        }

        // 닉네임 변경
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                log.error("Nickname already in use: {}", request.getNickname());
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.changeNickname(request.getNickname());
            log.debug("Nickname updated for user ID: {}", userId);
        }

        return user;
    }

    /**
     * 사용자 삭제
     * @param userId
     */
    @Override
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

    /**
     * 로그아웃
     * @param token
     */
    /**
     * 사용자 로그아웃 (ID 기반)
     * @param userId
     */
    @Override
    @Transactional
    public void logout(Long userId) { // 원래 String token 삭제하는 걸로 했는데
    	// 만약 다중 로그인일 경우 한 기기의 토큰 보다 모든 기기에서의 로그아웃을 해야 함
    	// 그러므로 단일 토큰 보다 한 사용자의 모든 토큰을 삭제하는 것이 더 바람직함.
        log.info("Request to logout for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.error("User not found for ID: {}", userId);
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        refreshTokenRepository.deleteByUserId(userId);
        log.debug("All refresh tokens invalidated for user ID: {}", userId);
    }


    /**
     * 모든 사용자 조회 (관리자 전용)
     * @return List<User>
     */
    @Transactional
    public List<User> findAllUsers() {
        log.info("Request to find all users");
        return userRepository.findAll();
    }
}
