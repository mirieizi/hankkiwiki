package com.hankki.domain.recommend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 사용자의 식사 추천 로그(스푼 사용 확인 용도)
 */
@Entity
@Table(name = "user_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int recommendationCount;

    public void increase() {
        this.recommendationCount++;
    }

    public boolean isExceeded(int maxCount) {
        return this.recommendationCount >= maxCount;
    }

    @PrePersist
    public void prePersist() {
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }
}
