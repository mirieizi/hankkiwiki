package com.hankki.domain.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hankki.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 스프링 시큐리티에서 사용자 인증 정보를 가져오는 서비스
 */
@RequiredArgsConstructor
@Service
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + email)
            );
    }
}
