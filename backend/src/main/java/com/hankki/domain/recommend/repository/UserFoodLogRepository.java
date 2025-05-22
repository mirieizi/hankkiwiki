package com.hankki.domain.recommend.repository;

import com.hankki.domain.recommend.entity.UserFoodLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface UserFoodLogRepository extends JpaRepository <UserFoodLog, Long> {

    @Query("SELECT u.foodId FROM UserFoodLog u WHERE u.userId = :userId AND u.date BETWEEN :start AND :end")
    List<Long> findRecommendedFoodIdsByUserIdAndTakeAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
