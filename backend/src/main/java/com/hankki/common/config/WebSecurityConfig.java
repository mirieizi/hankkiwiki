package com.hankki.common.config;

import java.util.Arrays;
import java.util.List;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hankki.common.security.filter.TokenAuthenticationFilter;
import com.hankki.common.security.jwt.TokenProvider;
import com.hankki.domain.auth.service.AuthUserDetailsService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig {
    private final AuthUserDetailsService authUserDetailsService;
    private final TokenProvider tokenProvider;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 정적 리소스 및 H2 콘솔 무시
        return web -> web.ignoring()
            .requestMatchers("/static/**", "/h2-console/**");
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//            .csrf(csrf -> csrf.disable())
//            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .authorizeHttpRequests(authz -> authz
//                .requestMatchers("/api/user/admin/**").hasRole("ADMIN")
//                .requestMatchers("/api/actuator/health", "/api/actuator/info").permitAll()
//                // 로그인, 회원가입, 토큰 갱신 등은 모두 허용
//                .requestMatchers(
//                    "/user/signup",
//                    "/user/login",
//                    "/user/refresh"
//                ).permitAll()
//                .anyRequest().authenticated()
//            )
//            .addFilterBefore(
//                new TokenAuthenticationFilter(tokenProvider),
//                UsernamePasswordAuthenticationFilter.class
//            );
//
//        return http.build();
//    }
    
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//          // ↓↓↓ 모든 요청을 인증 없이 허용
//          .authorizeHttpRequests(authz -> authz
//               .anyRequest().permitAll()
//          )
//          .csrf(csrf -> csrf.disable())
//          .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//          .cors(cors -> cors.configurationSource(corsConfigurationSource()));
//        return http.build();
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(csrf -> csrf.disable())
          .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(authz -> authz
              // 로그인·회원가입·토큰 갱신은 모두 인증 없이 접근 허용
              .requestMatchers(
                  "/auth/signup",
                  "/auth/login",
                  "/auth/refresh",
                  "/swagger-ui/**",
                  "/v3/api-docs/**",
                  "/swagger-resources/**",
                  "/webjars/**",
                  "/swagger/**"
              ).permitAll()
              // 그 외 모든 요청은 인증 필요
              .anyRequest().authenticated()
          )
          // JWT 토큰 필터 등록
          .addFilterBefore(
              new TokenAuthenticationFilter(tokenProvider),
              UsernamePasswordAuthenticationFilter.class
          );

        return http.build();
    }
  
    
    
    
    

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
            .userDetailsService(authUserDetailsService)
            .passwordEncoder(bCryptPasswordEncoder());
        return authBuilder.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        // CRUD + OPTIONS, PATCH 요청 허용
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        // 클라이언트가 Authorization, Content-Type 헤더를 읽을 수 있도록
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        // 토큰만 씀
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
