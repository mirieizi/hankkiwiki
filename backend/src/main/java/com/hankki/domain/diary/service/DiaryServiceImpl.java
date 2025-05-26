package com.hankki.domain.diary.service;

import java.util.List;

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


@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {
    private final DiaryRepository diaryRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public Long createDiary(Long userId, DiaryCreateRequest request) {
        Diary diary = Diary.builder()
            .user(User.builder().id(userId).build())
            .date(request.getDate())
            .content(request.getContent())
            .build();

        Long savedId = diaryRepository.save(diary).getId();
        return savedId;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public List<DiaryResponse> getDiariesByUserId(Long userId) {
        return diaryRepository.findResponsesByUserIdOrderByDateAsc(userId);
    }

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

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public void deleteDiary(Long userId, Long diaryId) {
        // 삭제가 성공하면 아무 예외도 발생하지 않음
        int deletedCount = diaryRepository.deleteByIdAndUserId(diaryId, userId);
        
        if (deletedCount == 0) {
            throw new IllegalArgumentException("Diary not found or access denied");
        }
    }
}
