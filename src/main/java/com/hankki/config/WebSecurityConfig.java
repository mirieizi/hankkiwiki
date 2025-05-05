package com.hankki.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.hankki.domain.user.service.UserDetailService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig {
    private final UserDetailService userService;

    @Bean
    public WebSecurityCustomizer configure() {
        // H2 콘솔 및 정적 리소스 보안 필터 제외
        return web -> web.ignoring()
            .requestMatchers(toH2Console())
            .requestMatchers("/static/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // authorizeRequests() deprecated, authorizeHttpRequests() 사용
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/signup", "/user").permitAll()
                .anyRequest().authenticated()
            )
            // 폼 로그인 설정
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            // 로그아웃 설정
            .logout(logout -> logout
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
            )
            // CSRF 비활성화
            .csrf(csrf -> csrf.disable())
            // H2 콘솔 프레임 옵션 비활성화
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            BCryptPasswordEncoder bCryptPasswordEncoder
    ) throws Exception {
        AuthenticationManagerBuilder authBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        // UserDetailService로 사용자 인증 설정
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
