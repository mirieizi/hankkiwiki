package com.hankki.domain.user.service;

import java.util.List;

import com.hankki.domain.auth.dto.request.LoginRequest;
import com.hankki.domain.auth.dto.request.SignUpRequest;
import com.hankki.domain.auth.dto.response.JwtTokenResponse;
import com.hankki.domain.user.dto.UpdateUserRequest;
import com.hankki.domain.user.entity.User;

/**
 * 사용자 회원가입, 조회, 수정, 삭제 로직을 담당하는 서비스
 */
public interface UserService {
    /** 회원가입 처리 */
    Long signUp(SignUpRequest request);

    /** ID로 사용자 조회 */
    User findById(Long userId);

    /** 사용자 정보 수정 */
    User updateUser(Long userId, UpdateUserRequest request);

    /** 사용자 삭제 */
    void deleteUser(Long userId);

    /** 사용자 로그인 */
	JwtTokenResponse login(LoginRequest request);
	
    /** 사용자 로그아웃 (ID 기반)*/
    void logout(Long userId);
    
    /** 모든 사용자 조회 (관리자 전용) */
    List<User> findAllUsers();
}
