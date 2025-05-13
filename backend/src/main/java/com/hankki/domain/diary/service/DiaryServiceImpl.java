package com.hankki.domain.diary.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hankki.domain.diary.dto.DiaryCreateRequest;
import com.hankki.domain.diary.dto.DiaryResponse;
import com.hankki.domain.diary.dto.DiaryUpdateRequest;
import com.hankki.domain.diary.entity.Diary;
import com.hankki.domain.diary.repository.DiaryRepository;
import com.hankki.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * 다이어리 엔트리의 비즈니스 로직을 처리하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {
    private final DiaryRepository diaryRepository;

    /**
     * 주어진 사용자에 대해 새로운 다이어리 엔트리를 생성합니다.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public Long createDiary(Long userId, DiaryCreateRequest request) {
        Diary diary = Diary.builder()
            .user(User.builder().id(userId).build())
            .date(request.getDate())
            .content(request.getContent())
            .build();
        return diaryRepository.save(diary).getId();
    }



    /**
     * 주어진 사용자와 다이어리 ID로 특정 다이어리 엔트리를 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public DiaryResponse getDiaryById(Long userId, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
            .filter(d -> d.getUser().getId().equals(userId))
            .orElseThrow(() -> new IllegalArgumentException("Diary not found or access denied"));
        return DiaryResponse.builder()
            .id(diary.getId())
            .date(diary.getDate())
            .content(diary.getContent())
            .build();
    }

    /**
     * 주어진 사용자와 다이어리 ID로 다이어리 엔트리를 수정합니다.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public DiaryResponse updateDiary(Long userId, Long diaryId, DiaryUpdateRequest request) {
        Diary diary = diaryRepository.findById(diaryId)
            .orElseThrow(() -> new IllegalArgumentException("Diary not found: " + diaryId));
        if (!diary.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }
        diary.changeDate(request.getDate());
        diary.changeContent(request.getContent());
        return DiaryResponse.builder()
            .id(diary.getId())
            .date(diary.getDate())
            .content(diary.getContent())
            .build();
    }

    /**
     * 주어진 사용자와 다이어리 ID로 다이어리 엔트리를 삭제합니다.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public void deleteDiary(Long userId, Long diaryId) {
        diaryRepository.deleteByIdAndUserId(diaryId, userId);
    }
}
