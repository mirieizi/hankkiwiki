package com.hankki.domain.diet.repository;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.entity.Diet;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DietRepository  extends JpaRepository<Diet, Long> {

    List<Diet> findDietsByUserId(Long userId);

    List<Diet> findDietsByUserIdAndTakeAt(Long userId, LocalDate takeAt);

    boolean existsByUserIdAndTakeAtAndMealType(Long userId, LocalDate takeAt, MealType mealType);
}
