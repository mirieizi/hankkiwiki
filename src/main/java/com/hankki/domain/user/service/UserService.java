package com.hankki.domain.user.service;

import com.hankki.domain.user.dto.SignUpRequest;
import com.hankki.domain.user.entity.User;

public interface UserService {
    /**
     * 회원가입 처리
     * @param request SignUpRequest
     * @return 생성된 사용자 ID
     */
    Long signUp(SignUpRequest request);

    /**
     * ID 로 사용자 조회
     * @param userId 사용자 PK
     * @return User 엔티티
     */
    User findById(Long userId);
}
