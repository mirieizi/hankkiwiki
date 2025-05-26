package com.hankki.domain.food.service;

import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * food_data.csv 파일을 읽어 MySQL의 food 테이블에 음식 정보를 초기화하는 클래스입니다.
 *
 * - Spring Boot 애플리케이션 시작 시 @PostConstruct를 통해 자동 실행됩니다.
 * - CSV 파일은 resources 디렉토리에 위치하며, 음식 이름, 분류, 영양소 등의 정보가 포함되어 있습니다.
 *
 * - 각 행은 Food 엔티티로 매핑되어 데이터베이스에 저장되며,
 *   이미 데이터가 존재하는지 여부는 따로 확인하지 않고 모두 삽입합니다.
 *
 * - 데이터 저장 중 문제가 발생하면 로그로 경고 또는 예외 메시지를 출력합니다.
 *
 * 이 클래스는 주로 개발 또는 초기 배포 단계에서 식품 데이터를 자동으로 삽입하기 위한 용도로 사용됩니다.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodInitializer {

    private static final String CSV_FILE_NAME = "/mini_food_data.csv";

    private final FoodRepository foodRepository;

    public void init() {
        try (InputStream is = getClass().getResourceAsStream(CSV_FILE_NAME);
             CSVReader reader = new CSVReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String[] tokens;
            reader.readNext(); // skip header

            while ((tokens = reader.readNext()) != null) {
                if (tokens.length < 12) {
                    log.warn("[FoodInitializer] 컬럼 수 부족 - skip: {}", String.join(",", tokens));
                    continue;
                }

                String foodName = tokens[0].replaceAll("[\\s_]", "").trim();
                if (foodRepository.findByFoodName(foodName).isPresent()) {
                    log.info("이미 존재: {}", foodName);
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

                foodRepository.save(food);
            }
            log.info("[FoodInitializer] Food Data 로드 성공");

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
