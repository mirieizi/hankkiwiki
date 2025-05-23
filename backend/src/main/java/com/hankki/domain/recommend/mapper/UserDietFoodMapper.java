package com.hankki.domain.recommend.mapper;

import com.hankki.domain.recommend.entity.UserDietFoodMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserDietFoodMapper {
    // 식단에 등록된 FoodId 목록 조회
    List<Long> findFoodIdIdsByDietId(@Param("dietId") Long dietId);

    // Diet에 Food 등록
    void insertUserDietFoodMap(UserDietFoodMap userDietFoodMap);

    // 특정 Diet에 연결된 모든 Food 삭제
    void deleteByDietId(@Param("dietId") Long dietId);

    // 특정 사용자의 3일간 음식 Id 조회
    List<Long> findFoodIdsByUserIdAndTakeAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

}
