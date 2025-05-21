package com.hankki.domain.recommend.entity;

import com.hankki.domain.diet.constant.MealType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 사용자가 추천받은 음식 로그(중복 추천 방지용)
 */
@Entity
@Table(name = "user_food_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @Column(name = "take_at", nullable = false)
    private LocalDate takeAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;
}
