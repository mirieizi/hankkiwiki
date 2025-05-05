package com.hankki.domain.diet.mapper;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.entity.Diet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DietMapper {
    void insertDiet(Diet diet);

    Diet getDietByDate(@Param("email") String email, @Param("takeAt") String takeAt,
                       @Param("mealType") MealType mealType);
}
