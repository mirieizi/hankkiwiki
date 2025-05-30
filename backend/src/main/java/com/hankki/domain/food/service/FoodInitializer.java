package com.hankki.domain.food.service;

import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodInitializer {

    private static final String CSV_FILE_NAME = "/mini_food_data.csv";
    private static final int BATCH_SIZE = 200;

    private final FoodRepository foodRepository;

    @PostConstruct
    public void init() {
        try (InputStream is = getClass().getResourceAsStream(CSV_FILE_NAME);
             CSVReader reader = new CSVReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            // 1. 이미 존재하는 음식명 미리 조회 (중복 체크 빠르게)
            Set<String> existingFoodNames = new HashSet<>();
            for (Food food : foodRepository.findAll()) {
                existingFoodNames.add(food.getFoodName());
            }

            String[] tokens;
            reader.readNext(); // skip header

            List<Food> batch = new ArrayList<>(BATCH_SIZE);
            int total = 0, skipped = 0, duplicated = 0, inserted = 0;

            while ((tokens = reader.readNext()) != null) {
                total++;
                if (tokens.length < 12) {
                    skipped++;
                    log.warn("[FoodInitializer] 컬럼 수 부족 - skip: {}", String.join(",", tokens));
                    continue;
                }

                String foodName = tokens[0].replaceAll("[\\s_]", "").trim();
                if (existingFoodNames.contains(foodName)) {
                    duplicated++;
                    // log.debug("이미 존재: {}", foodName); // 필요시만 활성화
                    continue;
                }

                Food food = Food.builder()
                        .foodName(foodName)
                        .majorCategory(tokens[1].trim())
                        .subCategory(tokens[2].trim())
                        .servingSize(safeParseDouble(tokens[3], 0))
                        .kcal(safeParseDouble(tokens[4], 0))
                        .moisture(safeParseDouble(tokens[5], 0))
                        .carbohydrate(safeParseDouble(tokens[6], 0))
                        .protein(safeParseDouble(tokens[7], 0))
                        .fat(safeParseDouble(tokens[8], 0))
                        .sugar(safeParseDouble(tokens[9], 0))
                        .sodium(safeParseDouble(tokens[10], 0))
                        .cholesterol(safeParseDouble(tokens[11], 0))
                        .build();

                batch.add(food);

                // BATCH_SIZE마다 일괄 저장
                if (batch.size() >= BATCH_SIZE) {
                    foodRepository.saveAll(batch);
                    inserted += batch.size();
                    batch.clear();
                }
            }
            // 남은 데이터 저장
            if (!batch.isEmpty()) {
                foodRepository.saveAll(batch);
                inserted += batch.size();
            }

            log.info("[FoodInitializer] Food Data 로드 완료: 전체={}, 중복={}, 컬럼부족={}, 신규삽입={}",
                    total, duplicated, skipped, inserted);

        } catch (Exception e) {
            log.warn("[FoodInitializer] Food Data 로드 실패: {}", e.getMessage(), e);
        }
    }

    private double safeParseDouble(String value, double defaultValue) {
        try {
            return value == null || value.trim().isEmpty()
                    ? defaultValue
                    : Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
