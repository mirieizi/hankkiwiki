package com.hankki.domain.food.service;

import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test") // 테스트 환경에서는 자동 실행 방지
public class FoodInitializer {

    private static final String CSV_FILE_NAME = "/mini_food_data.csv";
    private static final int BATCH_SIZE = 200;

    private final FoodRepository foodRepository;

    private volatile boolean isInitializing = false;

    @PostConstruct
    public void init() {
        if (isInitializing) {
            log.warn("[FoodInitializer] 이미 초기화 진행 중");
            return;
        }

        synchronized (this) {
            if (isInitializing) return;
            isInitializing = true;
        }

        try {
            if (foodRepository.count() > 0) {
                log.info("[FoodInitializer] 기존 데이터가 존재하여 초기화를 건너뜁니다.");
                return;
            }

            loadFoodDataFromCSV();

        } finally {
            isInitializing = false;
        }
    }

    @Transactional
    public void loadFoodDataFromCSV() {
        try (InputStream is = getClass().getResourceAsStream(CSV_FILE_NAME)) {
            if (is == null) {
                log.error("[FoodInitializer] CSV 파일을 찾을 수 없습니다: {}", CSV_FILE_NAME);
                throw new IllegalStateException("CSV 파일 없음: " + CSV_FILE_NAME);
            }

            try (CSVReader reader = new CSVReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                processCSVData(reader);
            }

        } catch (Exception e) {
            log.error("[FoodInitializer] Food Data 로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Food 데이터 초기화 실패", e);
        }
    }

    private void processCSVData(CSVReader reader) throws Exception {
        Set<String> existingFoodNames = new HashSet<>(foodRepository.findAllFoodNames());

        String[] tokens;
        reader.readNext(); // skip header

        List<Food> batch = new ArrayList<>(BATCH_SIZE);
        int total = 0, skipped = 0, duplicated = 0, inserted = 0;

        while ((tokens = reader.readNext()) != null) {
            total++;

            Optional<Food> foodOpt = parseCSVRow(tokens);
            if (foodOpt.isEmpty()) {
                skipped++;
                continue;
            }

            Food food = foodOpt.get();
            if (existingFoodNames.contains(food.getFoodName())) {
                duplicated++;
                continue;
            }

            batch.add(food);
            existingFoodNames.add(food.getFoodName());

            if (batch.size() >= BATCH_SIZE) {
                int saved = saveBatchSafely(batch);
                inserted += saved;
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            inserted += saveBatchSafely(batch);
        }

        log.info("[FoodInitializer] 초기화 완료: 총={}, 중복={}, 컬럼부족={}, 신규삽입={}",
                total, duplicated, skipped, inserted);
    }

    private int saveBatchSafely(List<Food> foods) {
        try {
            foodRepository.saveAll(foods);
            log.debug("[FoodInitializer] 배치 저장 성공: {} 개", foods.size());
            return foods.size();

        } catch (DataIntegrityViolationException e) {
            log.warn("[FoodInitializer] 배치 저장 실패 - 개별 저장으로 전환: {}", e.getMessage());
            return saveIndividually(foods);
        }
    }

    private int saveIndividually(List<Food> foods) {
        int count = 0;
        for (Food food : foods) {
            try {
                if (!foodRepository.existsByFoodName(food.getFoodName())) {
                    foodRepository.save(food);
                    count++;
                } else {
                    log.debug("[FoodInitializer] 개별 저장 중 중복 발견: {}", food.getFoodName());
                }
            } catch (DataIntegrityViolationException e) {
                log.debug("[FoodInitializer] 개별 저장 실패(중복): {}", food.getFoodName());
            } catch (Exception e) {
                log.warn("[FoodInitializer] 개별 저장 실패: {} - {}", food.getFoodName(), e.getMessage());
            }
        }
        log.info("[FoodInitializer] 개별 저장 완료: {}/{} 성공", count, foods.size());
        return count;
    }

    private Optional<Food> parseCSVRow(String[] tokens) {
        if (tokens.length < 12) {
            log.warn("[FoodInitializer] 컬럼 부족 - skip: {}", String.join(",", tokens));
            return Optional.empty();
        }

        try {
            String foodName = tokens[0].replaceAll("[\\s_]", "").trim();
            if (foodName.isEmpty()) {
                log.warn("[FoodInitializer] 음식명이 비어 있음 - skip");
                return Optional.empty();
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

            return Optional.of(food);

        } catch (Exception e) {
            log.warn("[FoodInitializer] 파싱 실패: {} - {}", String.join(",", tokens), e.getMessage());
            return Optional.empty();
        }
    }

    private double safeParseDouble(String value, double defaultValue) {
        try {
            return (value == null || value.trim().isEmpty())
                    ? defaultValue
                    : Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 수동 초기화 메서드 (관리자/개발자 전용)
     * 테스트 또는 배포 후 수동 호출용으로만 사용하세요.
     */
    public void manualInit() {
        log.info("[FoodInitializer] 수동 초기화 시작");
        init();
    }
}
