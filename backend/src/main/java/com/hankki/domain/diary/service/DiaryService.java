package com.hankki.domain.diary.service;

import java.util.List;

import com.hankki.domain.diary.dto.DiaryCreateRequest;
import com.hankki.domain.diary.dto.DiaryResponse;
import com.hankki.domain.diary.dto.DiaryUpdateRequest;

/**
 * 다이어리 엔트리의 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface DiaryService {
    /**
     * 주어진 사용자에 대해 새로운 다이어리 엔트리를 생성
     * @param userId 사용자 식별자
     * @param request 생성 요청 DTO
     * @return 생성된 다이어리 엔트리의 ID
     */
    Long createDiary(Long userId, DiaryCreateRequest request);

    /**
     * 주어진 사용자와 다이어리 ID로 특정 다이어리 엔트리를 조회
     * @param userId 사용자 식별자
     * @param diaryId 다이어리 식별자
     * @return 다이어리 응답 DTO
     */
    DiaryResponse getDiaryById(Long userId, Long diaryId);

    /**
     * 주어진 사용자와 다이어리 ID로 다이어리 엔트리를 수정
     * @param userId 사용자 식별자
     * @param diaryId 다이어리 식별자
     * @param request 수정 요청 DTO
     * @return 수정된 다이어리 응답 DTO
     */
    DiaryResponse updateDiary(Long userId, Long diaryId, DiaryUpdateRequest request);

    /**
     * 주어진 사용자와 다이어리 ID로 다이어리 엔트리를 삭제
     * @param userId 사용자 식별자
     * @param diaryId 다이어리 식별자
     */
    void deleteDiary(Long userId, Long diaryId);

	/**
	 * 주어진 사용자 ID로 전체 다이어리 목록을 조회합니다.
	 */
	List<DiaryResponse> getDiariesByUserId(Long userId);
}
