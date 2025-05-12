package com.hankki.domain.diet.mapper;

import com.hankki.domain.diet.entity.Diet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DietMapper {
    void insertDiet(Diet diet);
    List<Diet> getDietByTakeAt(@Param("email") String email, @Param("takeAt") LocalDate takeAt);
    Optional<Diet> getDietById(@Param("id") Long id);
    void deleteDietById(@Param("dietId") Long dietId);
    void updateDietInfo(Diet diet);
}
