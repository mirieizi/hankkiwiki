package com.hankki.domain.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.hankki.domain.user.dto.SignUpRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.service.UserServiceImpl;
import com.hankki.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserSignupTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("signUp(): 정상 요청시 비밀번호 인코딩 후 저장하고 새로운 ID 리턴")
    void signUp_success() {
        // given
        SignUpRequest req = new SignUpRequest("alice@example.com", "rawPass", "Alice");
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");

        User saved = User.builder()
                        .id(42L)
                        .email("alice@example.com")
                        .password("encodedPass")
                        .nickname("Alice")
                        .build();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        // when
        Long newId = userService.signUp(req);

        // 콘솔 출력
        System.out.println("생성된 사용자 ID: " + newId);

        // then
        assertEquals(42L, newId);
        verify(passwordEncoder).encode("rawPass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("findById(): 존재하는 ID면 User 반환")
    void findById_found() {
        // given
        User existing = User.builder()
                            .id(7L)
                            .email("bob@example.com")
                            .password("pw")
                            .nickname("Bob")
                            .build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(existing));

        // when
        User result = userService.findById(7L);

        // 콘솔 출력
        System.out.println("조회된 사용자: " + result);

        // then
        assertSame(existing, result);
    }

    @Test
    @DisplayName("findById(): 없는 ID면 IllegalArgumentException 발생")
    void findById_notFound() {
        // given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            userService.findById(99L);
        });

        // 콘솔 출력
        System.out.println("예외 메시지: " + e.getMessage());
    }
}
