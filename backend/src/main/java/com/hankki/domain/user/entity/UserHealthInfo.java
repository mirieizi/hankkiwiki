package com.hankki.domain.user.entity;

import com.hankki.domain.user.constant.ActivityFactor;
import com.hankki.domain.user.constant.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "user_health_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UserHealthInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Gender gender;

    @NotNull
    @Min(value = 50, message = "키는 최소 50cm 이상이어야 합니다.")
    @Max(value = 300, message = "키는 최대 300cm 이하여야 합니다.")
    @Column(nullable = false)
    private Integer height;

    @NotNull
    @Min(value = 1, message = "몸무게는 1kg 이상이어야 합니다.")
    @Max(value = 500, message = "몸무게는 최대 500kg 이하여야 합니다.")
    @Column(nullable = false)
    private Integer weight;

    @NotNull
    @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
    @Max(value = 150, message = "나이는 최대 150 이하여야 합니다.")
    @Column(nullable = false)
    private Integer age;

    @NotNull
    @Column(name = "activity_factor", nullable = false)
    private ActivityFactor activityFactor;

    @OneToOne(fetch=FetchType.LAZY) // FK 소유하는 엔티티 lazy 로딩 설정
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 저장된 일일 권장 칼로리 (자동 갱신)
    @Column(name = "recommended_calorie", nullable = false)
    private Double recommendedCalorie;

    /**
     * 엔티티 생성 전/수정 전마다 자동으로 호출되어 권장 칼로리 업데이트
     */
    @PrePersist
    @PreUpdate
    private void updateRecommendedCalorie() {
        double bmr = 10 * weight + 6.25 * height - 5 * age;
        if (gender == Gender.MALE) {
            bmr += 5;
        } else {
            bmr -= 161;
        }
        this.recommendedCalorie = bmr * activityFactor.getCoefficient();
    }

    /** BMR 계산 (Mifflin–St Jeor 공식 기반) */
    public double calculateBmr() {
        double bmr = 10 * weight + 6.25 * height - 5 * age;
        if (gender == Gender.MALE) {
            bmr += 5;
        } else {
            bmr -= 161;
        }
        return bmr;
    }

    /** 일일 권장 칼로리 (BMR × 활동 계수) */
    public double calculateDailyCalorie() {
        return calculateBmr() * activityFactor.getCoefficient();
    }

    /** 키 변경 */
    public void changeHeight(Integer height) {
        this.height = height;
    }

    /** 몸무게 변경 */
    public void changeWeight(Integer weight) {
        this.weight = weight;
    }

    /** 나이 변경 */
    public void changeAge(Integer age) {
        this.age = age;
    }

    /** 성별 변경 */
    public void changeGender(Gender gender) {
        this.gender = gender;
    }

    /** 활동 레벨 변경 */
    public void changeActivityFactor(ActivityFactor activityFactor) {
        this.activityFactor = activityFactor;
    }

}