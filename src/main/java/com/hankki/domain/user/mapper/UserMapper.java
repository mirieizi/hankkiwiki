package com.hankki.domain.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hankki.domain.user.entity.User;

@Mapper
public interface UserMapper {
    // 1) 단일 조회
    User selectById(@Param("id") Long id);

    // 2) 여러 건 조회
    List<User> selectAll();

    // 3) 삽입
    int insert(User user);

    // 4) 수정
    int update(User user);

    // 5) 삭제 (soft delete 등)
    int deleteById(@Param("id") Long id);

	User selectByEmail(String username);
}
