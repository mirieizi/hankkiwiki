package com.hankki.domain.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.hankki.domain.user.dto.SignUpRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 회원가입 로직을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 회원가입 처리
     * @param request SignUpRequest
     * @return 생성된 사용자 ID
     */
    @Transactional
    public Long signUp(SignUpRequest request) {
        // 엔티티 생성 및 저장
        User user = User.builder()
            .email(request.getEmail())
            .password(bCryptPasswordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .build();
        return userRepository.save(user).getId();
    }
    
    public User findById(Long userId) {
    	return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("unexpected User"));
    }
}