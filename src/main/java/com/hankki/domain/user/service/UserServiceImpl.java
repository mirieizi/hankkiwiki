package com.hankki.domain.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.hankki.domain.user.dto.SignUpRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    public Long signUp(SignUpRequest request) {
        User user = User.builder()
            .email(request.getEmail())
            .password(bCryptPasswordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .build();
        return userRepository.save(user).getId();
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("unexpected User"));
    }
}
