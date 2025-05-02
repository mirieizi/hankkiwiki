package com.hankki.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      // CSRF는 기본적으로 활성화되어 있지만, API 전용이면 비활성화 가능
      .csrf(csrf -> csrf.disable())

      // 모든 요청은 인증된 사용자만 접근 허용
      .authorizeHttpRequests(auth -> auth
        .anyRequest().authenticated()
      )

      // 1) 기본 로그인 폼 활성화 (스프링이 /login 페이지 제공)
      .formLogin(Customizer.withDefaults())

      // 2) HTTP Basic 활성화 (curl 등 cli 테스트용)
      .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
