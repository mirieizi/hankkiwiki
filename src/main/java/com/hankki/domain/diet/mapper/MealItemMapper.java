package com.hankki.domain.diet.mapper;

import com.hankki.domain.diet.entity.MealItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MealItemMapper {
    List<MealItem> findPreviewByIds(@Param("ids") List<Long> ids);
}
