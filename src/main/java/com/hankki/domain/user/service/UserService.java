package com.hankki.domain.user.service;

import com.hankki.domain.user.dto.SignUpRequest;
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
    User updateUser(UpdateUserRequest request);

    /** 사용자 삭제 */
    void deleteUser(Long userId);
}