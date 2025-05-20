package com.hankki.domain.recommend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
}
