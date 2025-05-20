package com.hankki.domain.food.service;

import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
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
@Component
@RequiredArgsConstructor
public class FoodInitializer {

    private final FoodRepository foodRepository;

    @PostConstruct
    public void init() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/food_data.csv"), StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");

                Food food = Food.builder()
                        .foodName(tokens[0])
                        .majorCategory(tokens[1])
                        .subCategory(tokens[2])
                        .amountStandard(Integer.parseInt(tokens[3]))
                        .kcal(Integer.parseInt(tokens[4]))
                        .moisture(Integer.parseInt(tokens[5]))
                        .carbohydrate(Double.parseDouble(tokens[6]))
                        .protein(Double.parseDouble(tokens[7]))
                        .fat(Double.parseDouble(tokens[8]))
                        .sugar(Double.parseDouble(tokens[9]))
                        .sodium(Double.parseDouble(tokens[10]))
                        .cholesterol(Double.parseDouble(tokens[11]))
                        .build();

                foodRepository.save(food);
            }
            log.info("[FoodInitializer] Food Data 로드 성공");
        } catch (Exception e) {
            log.warn("[FoodInitializer] Food Date 로드 실패: {}", e.getMessage(), e);
        }

    }
}
