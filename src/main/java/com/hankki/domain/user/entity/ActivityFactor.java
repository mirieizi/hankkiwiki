package com.hankki.domain.user.entity;

public enum ActivityFactor {
    SEDENTARY(0.20),    // 거의 활동 없음 / 운동 안 함
    LIGHT(0.375),       // 가벼운 운동 (주 1–3회)
    MODERATE(0.555),    // 보통 이상의 활동 (주 3–5회)
    ACTIVE(0.725),      // 적극적 활동 (주 6–7회)
    VERY_ACTIVE(0.90);  // 운동선수 / 고강도 운동

    private final double coefficient;

    ActivityFactor(double coefficient) {
        this.coefficient = coefficient;
    }

    public double getCoefficient() {
        return coefficient;
    }
}