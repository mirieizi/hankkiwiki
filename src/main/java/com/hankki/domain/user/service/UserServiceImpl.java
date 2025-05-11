package com.hankki.domain.user.service;

import com.hankki.domain.user.dto.SignUpRequest;
import com.hankki.domain.user.dto.UpdateUserRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 사용자 회원가입, 조회, 수정, 삭제 로직을 담당하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        log.info("Request to find user by ID: {}", userId);
        return userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with ID: {}", userId);
                return new IllegalArgumentException("User not found: " + userId);
            });
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #request.userId == authentication.principal.id")
    @Transactional
    public User updateUser(UpdateUserRequest request) {
        log.info("Request to update user ID: {}", request.getUserId());
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> {
                log.error("Cannot update, user not found ID: {}", request.getUserId());
                return new IllegalArgumentException("User not found: " + request.getUserId());
            });
        if (request.getPassword() != null) {
            user.changePassword(request.getPassword(), passwordEncoder);
            log.debug("Password updated for user ID: {}", user.getId());
        }
        if (request.getNickname() != null) {
            user.changeNickname(request.getNickname());
            log.debug("Nickname updated for user ID: {}", user.getId());
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
}
