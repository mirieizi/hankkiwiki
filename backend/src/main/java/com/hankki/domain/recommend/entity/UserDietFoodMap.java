package com.hankki.domain.recommend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 섭취한 식단, 음식 중간 테이블
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_diet_food_map")
public class UserDietFoodMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "diet_id", nullable = false)
    private Long dietId;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

}
