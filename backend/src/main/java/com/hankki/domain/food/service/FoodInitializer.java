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
