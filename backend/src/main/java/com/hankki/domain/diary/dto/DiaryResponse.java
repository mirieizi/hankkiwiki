package com.hankki.domain.diary.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryResponse {
    /** 다이어리 고유 ID */
    private Long id;

    /** 기록 날짜 */
    private LocalDate date;

    /** 다이어리 내용 */
    private String content;
}
