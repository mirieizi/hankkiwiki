package com.hankki.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hankki.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    boolean existsByEmail(String email); //메서드 이름을 접두사 + 조건 형태로 해석하여 자동으로 SQL 쿼리를 생성
    boolean existsByNickname(String nickname);
    boolean existsById(Long userId);
    void deleteByEmail(String email);
    void deleteByNickname(String nickname);
    

}
