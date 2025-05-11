package com.hankki.domain.diet.mapper;

import com.hankki.domain.diet.entity.DietMealItem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DietMealItemMapper {

    void insertDietMealItem(DietMealItem dietMealItem);
    List<Long> findMealItemIdsByDietId(@Param("dietId")Long dietId);
    int deleteByDietId(@Param("dietId") Long dietId);
}
