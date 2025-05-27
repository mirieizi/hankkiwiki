package com.hankki.domain.diet.repository;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.entity.DietGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DietGroupRepository extends JpaRepository<DietGroup, Long> {

    List<DietGroup> findDietsByUserId(Long userId);

    List<DietGroup> findDietsByUserIdAndTakeAt(Long userId, LocalDate takeAt);

    DietGroup findByUserIdAndTakeAtAndMealType(Long userId, LocalDate takeAt, MealType mealType);

    @Query("SELECT dg.id FROM DietGroup dg WHERE dg.userId = :userId AND dg.takeAt BETWEEN :start AND :end")
    List<Long> findIdsByUserIdAndTakeAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    
    boolean existsByUserIdAndTakeAtBetween(Long userId, LocalDate from, LocalDate to);

    boolean existsByUserIdAndTakeAt(Long userId, LocalDate takeAt);
    List<DietGroup> findDietsByUserIdAndTakeAtBetween(Long userId, LocalDate from, LocalDate to);

}
