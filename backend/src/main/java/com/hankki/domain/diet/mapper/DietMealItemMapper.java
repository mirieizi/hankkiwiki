package com.hankki.domain.diet.mapper;

import com.hankki.domain.diet.entity.DietMealItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DietMealItemMapper {
    // 식단에 등록된 FoodId 목록 조회 (추천 등에서 사용)
    List<Long> findFoodIdIdsByDietId(@Param("dietId") Long dietId);

    // Diet에 MealItem 연결 (등록)
    void insertDietMealItem(DietMealItem dietMealItem);

    // 특정 Diet에 연결된 모든 MealItem 삭제
    void deleteByDietId(@Param("dietId") Long dietId);

}
