package com.hankki.domain.diary.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryUpdateRequest {
    /** 기록 날짜 (선택) */
    private LocalDate date;

    /** 다이어리 내용 */
    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 200, message = "내용은 최대 200자까지 가능합니다.")
    private String content;
}