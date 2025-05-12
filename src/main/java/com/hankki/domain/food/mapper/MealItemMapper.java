package com.hankki.domain.food.mapper;

import com.hankki.domain.food.entity.MealItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MealItemMapper {
    List<MealItem> findPreviewByIds(@Param("ids") List<Long> ids);
}
