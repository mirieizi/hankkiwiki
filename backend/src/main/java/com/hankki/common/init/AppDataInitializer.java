package com.hankki.common.init;

import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.vector.service.FoodVectorService;
import com.hankki.domain.food.service.FoodInitializer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppDataInitializer {

    private final FoodInitializer foodInitializer;
    private final FoodVectorService foodVectorService;
    private final FoodRepository foodRepository;

    @PostConstruct
    public void initAll() {
        log.info("[AppDataInitializer] 데이터 초기화 시작");

        try {
            // 1. 음식 데이터 초기화 및 검증
            initializeFoodData();

            // 2. 벡터 데이터 초기화
            initializeVectorData();

            log.info("[AppDataInitializer] 모든 데이터 초기화 완료");

        } catch (Exception e) {
            log.error("[AppDataInitializer] 데이터 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("애플리케이션 초기화 실패", e);
        }
    }

    private void initializeFoodData() {
        log.info("[AppDataInitializer] 음식 데이터 초기화 시작");

        try {
            foodInitializer.init();

            // 초기화 성공 여부 검증
            long foodCount = foodRepository.count();
            if (foodCount == 0) {
                throw new IllegalStateException("음식 데이터 로드 실패: 데이터가 없습니다");
            }

            log.info("[AppDataInitializer] 음식 데이터 초기화 완료: {} 개", foodCount);

        } catch (Exception e) {
            log.error("[AppDataInitializer] 음식 데이터 초기화 실패", e);
            throw e;
        }
    }

    private void initializeVectorData() {
        log.info("[AppDataInitializer] 벡터 데이터 초기화 시작");

        try {
            foodVectorService.loadVectors();
            log.info("[AppDataInitializer] 벡터 데이터 초기화 완료");

        } catch (Exception e) {
            log.error("[AppDataInitializer] 벡터 데이터 초기화 실패", e);
            throw e;
        }
    }
}
