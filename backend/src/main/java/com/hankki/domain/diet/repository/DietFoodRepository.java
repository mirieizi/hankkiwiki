package com.hankki.domain.diet.repository;

import com.hankki.domain.diet.entity.DietFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DietFoodRepository extends JpaRepository<DietFood, Long> {

    @Query("SELECT df.foodId FROM DietFood df WHERE df.dietGroupId = :groupId")
    List<Long> findFoodIdsByDietGroupId(@Param("groupId") Long dietGroupId);

    @Query("SELECT df.foodId FROM DietFood df WHERE df.dietGroupId IN :groupIds")
    List<Long> findFoodIdsByDietGroupIdIn(@Param("groupIds") List<Long> groupIds);

    boolean existsByDietGroupIdAndFoodId(Long id, Long foodId);

    void deleteByDietGroupId(Long dietGroupId);

}