// src/test/java/com/hankki/domain/user/entity/UserHealthInfoTest.java
package com.hankki.domain.user.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.hankki.domain.user.constant.ActivityFactor;
import com.hankki.domain.user.constant.Gender;

class UserHealthInfoTest {

    @Test
    void calculateBmr_male() {
        UserHealthInfo info = UserHealthInfo.builder()
            .height(180)
            .weight(75)
            .age(25)
            .gender(Gender.MALE)
            .activityFactor(ActivityFactor.LIGHT) // coefficient 0.375
            .build();

        // Mifflin–St Jeor 공식: 10*75 + 6.25*180 - 5*25 + 5 = 1673.0
        double expectedBmr = 10 * 75 + 6.25 * 180 - 5 * 25 + 5;
        assertThat(info.calculateBmr()).isEqualTo(expectedBmr);
        // 일일 권장 칼로리: BMR * 0.375
        assertThat(info.calculateDailyCalorie()).isEqualTo(expectedBmr * 0.375);
    }

    @Test
    void calculateBmr_female() {
        UserHealthInfo info = UserHealthInfo.builder()
            .height(160)
            .weight(60)
            .age(30)
            .gender(Gender.FEMALE)
            .activityFactor(ActivityFactor.MODERATE) // coefficient 0.555
            .build();

        // 공식: 10*60 + 6.25*160 - 5*30 - 161 = 1304.0
        double expectedBmr = 10 * 60 + 6.25 * 160 - 5 * 30 - 161;
        assertThat(info.calculateBmr()).isEqualTo(expectedBmr);
        assertThat(info.calculateDailyCalorie()).isEqualTo(expectedBmr * 0.555);
    }


}
