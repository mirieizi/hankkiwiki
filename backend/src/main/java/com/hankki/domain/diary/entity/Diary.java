package com.hankki.domain.diary.entity;

import java.time.LocalDate;

import com.hankki.domain.user.entity.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "diary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id", updatable = false)
    private Long id;

    @Column(name = "take_at", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", length = 200)
    @Size(max = 200)
    private String content;

    /**
     * 엔티티 생성/수정 전 기본값 세팅
     */
    @PrePersist
    private void prePersist() {
        if (date == null) {
            date = LocalDate.now();
        }
    }

    /**
     * 다이어리 날짜 변경
     */
    public void changeDate(LocalDate date) {
        this.date = date;
    }

    /**
     * 다이어리 내용 변경
     */
    public void changeContent(String content) {
        this.content = content;
    }
}
