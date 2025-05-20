package com.hankki.common.redis.service;

import com.hankki.common.redis.util.RedisVectorUtil;
import com.hankki.domain.food.repository.FoodRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodVectorService {

    private final FoodRepository foodRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void loadAllVectors() {
        loadVectors("/vectors/male_vectors.csv", "male");
        loadVectors("/vectors/female_vectors.csv", "female");
    }

    private void loadVectors(String resourcePath, String gender) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(resourcePath), StandardCharsets.UTF_8))) {

            String header = reader.readLine(); // skip header
            String line;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                String foodName = tokens[0].trim();
                float[] vector = new float[9];
                for (int i = 0; i < 9; i++) {
                    vector[i] = Float.parseFloat(tokens[i + 1]);
                }

                foodRepository.findByFoodName(foodName).ifPresentOrElse(food -> {
                    String redisKey = String.format("food_%s:%d", gender, food.getId());
                    byte[] vectorBytes = RedisVectorUtil.floatArrayToBytes(vector);
                    redisTemplate.opsForHash().put(redisKey, "vector", vectorBytes);
                }, () -> {
                    log.warn("[FoodVectorService] DB 조회 실패: {} - {}", gender, foodName);
                });
            }

            log.info("[FoodVectorService] DB 조회 성공: {}", gender);

        } catch (Exception e) {
            log.error("[FoodVectorService] DB 저장 실패: {} - {}", gender, e.getMessage(), e);
        }
    }

}
