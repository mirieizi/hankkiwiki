package com.hankki.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hankki.domain.diary.dto.DiaryResponse;
import com.hankki.domain.diary.entity.Diary;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    // 엔티티 대신 DTO 생성자 호출 프로젝션
    @Query("SELECT new com.hankki.domain.diary.dto.DiaryResponse(d.id, d.date, d.content) "
         + "FROM Diary d "
         + "WHERE d.user.id = :userId "
         + "ORDER BY d.date ASC")
    List<DiaryResponse> findResponsesByUserIdOrderByDateAsc(@Param("userId") Long userId);

    // 삭제 메서드는 그대로
    void deleteByIdAndUserId(Long diaryId, Long userId);
}
