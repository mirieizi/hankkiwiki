// src/test/java/com/hankki/domain/user/entity/UserHealthInfoPersistenceTest.java
package com.hankki.domain.user.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.hankki.domain.user.constant.ActivityFactor;
import com.hankki.domain.user.constant.Gender;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.entity.UserHealthInfo;

@DataJpaTest
//@AutoConfigureTestDatabase(replace = Replace.NONE)  // 실제 설정된 DB로 테스트하려면, 아니면 지우세요
class UserHealthInfoPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void persist할_때_권장칼로리가_자동계산된다() {
        // 1) 먼저 User 엔티티를 저장
        User user = User.builder()
            .email("test@example.com")
            .password("P@ssw0rd!")
            .nickname("tester")
            .healthInfo(null)      // 양방향(연관관계) 편의 메서드가 없으므로 null 로 두고
            .build();
        user = em.persistFlushFind(user);

        // 2) UserHealthInfo 를 user 에 연관지어 저장
        UserHealthInfo info = UserHealthInfo.builder()
            .user(user)
            .gender(Gender.MALE)
            .height(180)
            .weight(75)
            .age(25)
            .activityFactor(ActivityFactor.LIGHT) // coefficient = 0.375
            .build();

        // persist 시점에 @PrePersist 가 동작하여 recommendedCalorie 세팅됨
        info = em.persistFlushFind(info);

        double expectedBmr = 10 * 75 + 6.25 * 180 - 5 * 25 + 5;  // Mifflin–St Jeor 공식
        double expectedCalorie = expectedBmr * ActivityFactor.LIGHT.getCoefficient();
        assertThat(info.getRecommendedCalorie())
            .isEqualTo(expectedCalorie);
    }

    @Test
    void update할_때도_권장칼로리가_재계산된다() {
        // 1) User & UserHealthInfo 초기 저장
        User user = User.builder()
            .email("upd@example.com")
            .password("P@ssw0rd!")
            .nickname("updtester")
            .healthInfo(null)
            .build();
        user = em.persistFlushFind(user);

        UserHealthInfo info = UserHealthInfo.builder()
            .user(user)
            .gender(Gender.FEMALE)
            .height(160)
            .weight(60)
            .age(30)
            .activityFactor(ActivityFactor.MODERATE) // coefficient = 0.555
            .build();
        info = em.persistFlushFind(info);

        // 2) 필드 변경 후 flush() → @PreUpdate 동작
        info.changeWeight(80);
        em.flush();  // 변경 감지 → updateRecommendedCalorie 호출

        double newBmr = 10 * 80 + 6.25 * 160 - 5 * 30 - 161;
        double newCalorie = newBmr * ActivityFactor.MODERATE.getCoefficient();
        // 영속성 컨텍스트에 반영된 값을 다시 조회
        UserHealthInfo updated = em.find(UserHealthInfo.class, info.getId());

        assertThat(updated.getRecommendedCalorie())
            .isEqualTo(newCalorie);
    }
}
