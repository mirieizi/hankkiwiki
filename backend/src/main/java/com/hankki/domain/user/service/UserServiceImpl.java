package com.hankki.domain.user.service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hankki.domain.user.dto.UserProfileResponse;
import com.hankki.domain.user.dto.UpdateUserRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.repository.UserHealthInfoRepository;
import com.hankki.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 회원가입, 로그인, 조회, 수정, 삭제 로직을 담당하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserHealthInfoRepository userHealthInfoRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 모든 사용자 조회 (관리자 전용)
     * @return List<User>
     */
    @Transactional
    public List<User> findAllUsers() {
        log.info("Request to find all users");
        return userRepository.findAll();
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
     * @param request
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
        userHealthInfoRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
        log.debug("Deleted user with ID: {}", userId);
    }

    @Override
    @Transactional
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new UserProfileResponse(user.getNickname());
    }

}
