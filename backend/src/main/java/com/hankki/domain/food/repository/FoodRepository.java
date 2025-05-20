package com.hankki.domain.food.repository;

import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import com.hankki.domain.food.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    @Query("""
        SELECT new com.hankki.domain.food.dto.FoodPreviewResponseDto(
            f.id, f.foodName, f.majorCategory, f.kcal
        )
        FROM Food f
        WHERE f.id IN :ids
    """)
    List<FoodPreviewResponseDto> findPreviewsByIds(@Param("ids") List<Long> ids);
}
