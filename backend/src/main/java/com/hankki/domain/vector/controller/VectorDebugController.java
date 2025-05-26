package com.hankki.domain.vector.controller;

import com.hankki.domain.vector.service.FoodVectorService;
import com.hankki.domain.vector.util.RedisVectorSearcher;
import com.hankki.domain.user.constant.Gender;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug/vector")
@RequiredArgsConstructor
@Slf4j
public class VectorDebugController {

    private final FoodVectorService foodVectorService;
    private final RedisCommands<byte[], byte[]> redisCommands;
    private final RedisVectorSearcher redisVectorSearcher;

    @PostMapping("/reload")
    public Map<String, Object> reloadVectors() {
        Map<String, Object> result = new HashMap<>();
        try {
            foodVectorService.loadVectors();
            result.put("success", true);
            result.put("message", "벡터 로딩 완료");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            log.error("[VectorDebugController] 벡터 로딩 실패", e);
        }
        return result;
    }

    @GetMapping("/check/{gender}/{foodId}")
    public Map<String, Object> checkVector(@PathVariable String gender, @PathVariable Long foodId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String redisKey = String.format("food_%s:%d", gender, foodId);
            byte[] vectorBytes = redisCommands.hget(
                    redisKey.getBytes(StandardCharsets.UTF_8),
                    "vector".getBytes(StandardCharsets.UTF_8)
            );

            result.put("key", redisKey);
            result.put("exists", vectorBytes != null);
            result.put("size", vectorBytes != null ? vectorBytes.length : 0);
            result.put("expectedSize", 9 * 4);
            result.put("valid", vectorBytes != null && vectorBytes.length == 9 * 4);

            if (vectorBytes != null) {
                // 첫 번째 값 확인
                try {
                    double[] vector = com.hankki.domain.vector.util.RedisVectorUtil.bytesToDoubleArray(vectorBytes);
                    result.put("firstValue", vector[0]);
                    result.put("vectorDimension", vector.length);
                } catch (Exception e) {
                    result.put("parseError", e.getMessage());
                }
            }

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return result;
    }

    @GetMapping("/test-search/{gender}")
    public Map<String, Object> testSearch(@PathVariable String gender,
                                          @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            Gender genderEnum = Gender.valueOf(gender.toUpperCase());

            // 테스트용 더미 벡터 (9차원)
            double[] testVector = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

            List<Long> knnResults = redisVectorSearcher.knnSearch(genderEnum, testVector, limit);
            List<Long> furthestResults = redisVectorSearcher.furthestSearch(genderEnum, testVector, limit);

            result.put("success", true);
            result.put("knnResults", knnResults);
            result.put("knnCount", knnResults.size());
            result.put("furthestResults", furthestResults);
            result.put("furthestCount", furthestResults.size());

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            log.error("[VectorDebugController] 검색 테스트 실패", e);
        }

        return result;
    }

    @GetMapping("/stats")
    public Map<String, Object> getVectorStats() {
        Map<String, Object> result = new HashMap<>();

        try {
            for (Gender gender : Gender.values()) {
                Map<String, Object> genderStats = new HashMap<>();

                // 샘플 키들 확인
                int existCount = 0;
                int totalCheck = 10;

                for (int i = 1; i <= totalCheck; i++) {
                    String redisKey = String.format("food_%s:%d", gender.key(), i);
                    byte[] vectorBytes = redisCommands.hget(
                            redisKey.getBytes(StandardCharsets.UTF_8),
                            "vector".getBytes(StandardCharsets.UTF_8)
                    );

                    if (vectorBytes != null && vectorBytes.length == 9 * 4) {
                        existCount++;
                    }
                }

                genderStats.put("sampleExistCount", existCount);
                genderStats.put("sampleTotalCheck", totalCheck);
                genderStats.put("sampleExistRate", (double) existCount / totalCheck);

                result.put(gender.key(), genderStats);
            }

            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
