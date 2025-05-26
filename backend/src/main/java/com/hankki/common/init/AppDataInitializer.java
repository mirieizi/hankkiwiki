package com.hankki.common.init;

import com.hankki.domain.vector.service.FoodVectorService;
import com.hankki.domain.food.service.FoodInitializer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppDataInitializer {

    private final FoodInitializer foodInitializer;
    private final FoodVectorService foodVectorService;

    @PostConstruct
    public void initAll() {
        foodInitializer.init();         // 먼저 DB 저장
        foodVectorService.loadVectors(); // 그다음 Redis 저장
    }
}
