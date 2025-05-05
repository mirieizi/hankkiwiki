
package com.hankki.config.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.hankki.domain.user.entity.User;

class TokenProviderTest {

    private TokenProvider tokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setIssuer("test-issuer");
        // HS256 시크릿 키는 최소 256비트(32바이트) 문자열
        jwtProperties.setSecretKey("01234567890123456789012345678901");

        tokenProvider = new TokenProvider(jwtProperties);
    }

    @Test
    void generateToken_and_validToken_shouldBeValid() {
        // given
        User user = User.builder()
            .email("alice@example.com")
            .password("ignored")
            .nickname("alice")
            .build();

        // when
        String token = tokenProvider.generateToken(user, Duration.ofHours(1));

        // then
        assertNotNull(token);
        assertTrue(tokenProvider.validToken(token));
    }

    @Test
    void validToken_expiredToken_shouldBeInvalid() throws InterruptedException {
        // given
        User user = User.builder()
            .email("bob@example.com")
            .password("ignored")
            .nickname("bob")
            .build();
        String token = tokenProvider.generateToken(user, Duration.ofMillis(1));
        Thread.sleep(10);

        // then
        assertFalse(tokenProvider.validToken(token));
    }

    @Test
    void getUserId_and_getAuthentication_shouldReturnCorrectValues() {
        // given
        User user = User.builder()
            .id(123L)
            .email("carol@example.com")
            .password("ignored")
            .nickname("carol")
            .build();
        String token = tokenProvider.generateToken(user, Duration.ofHours(1));

        // when
        Long extractedUserId = tokenProvider.getUserId(token);
        Authentication auth = tokenProvider.getAuthentication(token);

        // then
        assertEquals(123L, extractedUserId);
        assertEquals("carol@example.com", auth.getName());
        assertTrue(
            auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
        );
    }
}
