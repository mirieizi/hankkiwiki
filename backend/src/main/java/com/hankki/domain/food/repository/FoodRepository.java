package com.hankki.domain.food.repository;

import com.hankki.domain.food.dto.FoodPreviewResponseDto;
import com.hankki.domain.food.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long>, FoodRepositoryCustom {

    @Query("""
        SELECT new com.hankki.domain.food.dto.FoodPreviewResponseDto(
            f.id, f.foodName, f.majorCategory, f.kcal
        )
        FROM Food f
        WHERE f.id IN :ids
    """)
    List<FoodPreviewResponseDto> findPreviewsByIds(@Param("ids") List<Long> ids);

    Optional<Food> findByFoodName(String foodName);

    @Query(value = "SELECT * FROM food ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Food> findRandomFood();
    
    Optional<Food> findByFoodNameIgnoreCase(String name);
    
    // Full-text index 검색
    // MySQL이 텍스트 컬럼을 토큰화해서 색인해두는 구조. (ver 8이상부터 지원)
    // 설정 안되어 있으면 ERROR 1191 (HY000): Can't find FULLTEXT index matching the column list 오류
    @Query(value = """
            SELECT *
              FROM food f
             WHERE MATCH(f.food_name) AGAINST(:query IN NATURAL LANGUAGE MODE) > 1.0
             ORDER BY MATCH(f.food_name) AGAINST(:query IN NATURAL LANGUAGE MODE) DESC
             LIMIT 1
            """, nativeQuery = true)
    Optional<Food> findBestMatchByFullText(@Param("query") String query);

    List<Food> findByFoodNameContainingIgnoreCase(String query);

    @Query("SELECT f.foodName FROM Food f")
    List<String> findAllFoodNames();

    boolean existsByFoodName(String foodName);

}
