// src/main/java/com/hankki/config/WebSecurityConfig.java
package com.hankki.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hankki.config.TokenAuthenticationFilter;
import com.hankki.config.jwt.TokenProvider;
import com.hankki.domain.user.service.UserDetailService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig {
    private final UserDetailService  userService;
    private final TokenProvider      tokenProvider;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 정적 리소스 및 H2 콘솔 무시
        return web -> web.ignoring()
            .requestMatchers("/static/**", "/h2-console/**");
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // 관리자 전용 API는 ROLE_ADMIN 소유자만 접근 가능
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 로그인·회원가입·유저 조회·토큰 재발급은 모두 공개
                .requestMatchers(
                    "/login",
                    "/signup",
                    "/user/**",
                    "/token/refresh"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                new TokenAuthenticationFilter(tokenProvider),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            BCryptPasswordEncoder bCryptPasswordEncoder
    ) throws Exception {
        AuthenticationManagerBuilder authBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
            .userDetailsService(userService)
            .passwordEncoder(bCryptPasswordEncoder);
        return authBuilder.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
