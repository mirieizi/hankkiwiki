package com.hankki.domain.user.service;

import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User signup(User user) {
        // 비밀번호 암호화 후 저장
        user = User.builder()
                   .email(user.getEmail())
                   .password(passwordEncoder.encode(user.getPassword()))
                   .nickname(user.getNickname())
                   .dailyUsage(user.getDailyUsage())
                   .build();
        return userRepository.save(user);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                             .orElseThrow(() -> new UsernameNotFoundException("사용자 없음: " + id));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                                  .orElseThrow(() -> new UsernameNotFoundException("사용자 없음: " + username));
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .roles("USER")
            .build();
    }

	@Override
	public User login(User user) {
		// TODO Auto-generated method stub
		return null;
	}
}
