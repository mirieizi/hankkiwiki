package com.hankki.domain.recommend.repository;

import com.hankki.domain.recommend.entity.UserFoodLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFoodLogRepository extends JpaRepository <UserFoodLog, Long> {
    Optional<UserFoodLog> findTopByUserIdOrderByTakeAtDesc(Long userId);

    List<UserFoodLog> findAllByUserIdAndTakeAtBetween(Long userId, Long takeAtStart, Long takeAtEnd);
}
