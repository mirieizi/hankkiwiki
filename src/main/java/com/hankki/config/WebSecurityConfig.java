package com.hankki.config;

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
    public WebSecurityCustomizer webSecurityCustomizer() {
        // H2 콘솔 제외 설정 삭제됨 (필요 시 아래 줄 주석 해제)
//      .requestMatchers(toH2Console())         // ← 삭제된 라인, H2ConsoleProperties 오류 해결
        return web -> web.ignoring()
            .requestMatchers("/static/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(authz -> authz
             // 로그인 페이지와 회원가입은 누구나
             .requestMatchers("/login", "/login-error", "/user/signup").permitAll()
             .anyRequest().authenticated()
          )
          .formLogin(form -> form
             .loginPage("/login")            // ① GET /login → 로그인 폼 (아래에서 구현)
             .loginProcessingUrl("/login")   // ② POST /login → 필터가 아이디/비번 검사
             .usernameParameter("email")     // 폼 <input name="email">
             .passwordParameter("password")  // 폼 <input name="password">
             .defaultSuccessUrl("/", true)   // 로그인 성공 후
             .failureUrl("/login?error")     // 로그인 실패 후
          )
          .logout(logout -> logout
             .logoutUrl("/logout")           // POST /logout → 필터가 세션 무효화
             .logoutSuccessUrl("/login")
             .invalidateHttpSession(true)
             .deleteCookies("JSESSIONID")
             .permitAll()
          )
          .csrf(csrf -> csrf.disable());
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
