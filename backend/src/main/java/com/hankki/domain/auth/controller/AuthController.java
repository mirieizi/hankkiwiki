package com.hankki.domain.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hankki.common.security.principal.CurrentUser;
import com.hankki.domain.auth.dto.UserPrincipal;
import com.hankki.domain.auth.dto.request.LoginRequest;
import com.hankki.domain.auth.dto.request.SignUpRequest;
import com.hankki.domain.auth.dto.response.JwtTokenResponse;
import com.hankki.domain.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    /**
     * 회원 가입
     * POST /auth/signup
     */
    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<Void> signup(@RequestBody SignUpRequest request) {
        log.info("Request to sign up user: {}", request.getEmail());
        authService.signUp(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 로그인
     * POST /auth/login
     */
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<JwtTokenResponse> login(@RequestBody LoginRequest request) {
        log.info("Request to login user: {}", request.getEmail());
        JwtTokenResponse tokens = authService.login(request);
        return ResponseEntity.ok(tokens);
    }

    /**
     * 로그아웃
     * DELETE /auth/logout
     */
    @DeleteMapping("/logout")
    @Transactional
    public ResponseEntity<Void> logout(@CurrentUser UserPrincipal principal) {
        log.info("Request to logout token: {}", principal.getEmail());
        authService.logout(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
