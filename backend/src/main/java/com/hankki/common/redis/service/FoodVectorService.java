package com.hankki.common.redis.service;

import com.hankki.common.redis.util.RedisVectorUtil;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.user.constant.Gender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 벡터 CSV 파일을 읽고, 음식 정보와 매핑하여 Redis에 벡터 데이터를 저장하는 서비스입니다.
 *
 * - 벡터 파일은 성별(gender)에 따라 구분된 CSV 파일로 제공되며,
 *   각 행에는 food_name과 PCA 기반의 벡터 값(PC1~PC9)이 포함되어 있습니다.
 *
 * - CSV 파일의 food_name을 기준으로 MySQL의 food 테이블과 매칭한 뒤,
 *   해당 food.id를 Redis의 key로 사용하여 벡터를 저장합니다.
 *
 * - 저장된 Redis 키 형식은 다음과 같습니다:
 *   food_male:{foodId}, food_female:{foodId}
 *
 * - 저장되는 필드 이름은 "vector"이며, float[] 벡터는 float32 byte 배열로 변환되어 저장됩니다.
 *
 * - 애플리케이션 시작 시 자동으로 실행되며, 잘못된 데이터나 매핑 실패는 로그로 출력됩니다.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodVectorService {

    private static final int VECTOR_DIMENSION = 9;
    private static final String VECTOR_FILE_PATH = "/vectors/food_embeddings.csv";

    private final FoodRepository foodRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void loadVectors() {
        try (InputStream is = getClass().getResourceAsStream(VECTOR_FILE_PATH)) {
            if (is == null) {
                log.error("[FoodVectorService] 벡터 파일이 존재하지 않습니다: {}", VECTOR_FILE_PATH);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String line = reader.readLine(); // header

                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");

                    String foodName = tokens[0].replaceAll("[\\s_]", "").trim();
                    String genderStr = tokens[1].trim();

                    Gender gender;
                    try {
                        gender = Gender.valueOf(genderStr.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        log.warn("[FoodVectorService] 잘못된 gender 값: '{}'", genderStr);
                        continue;
                    }

                    double[] vector = new double[VECTOR_DIMENSION];
                    for (int i = 0; i < VECTOR_DIMENSION; i++) {
                        vector[i] = Double.parseDouble(tokens[i + 2]);
                    }

                    foodRepository.findByFoodName(foodName).ifPresentOrElse(food -> {
                        String redisKey = String.format("food_%s:%d", gender.key(), food.getId());
                        byte[] vectorBytes = RedisVectorUtil.doubleArrayToBytes(vector);
                        redisTemplate.opsForHash().put(redisKey, "vector", vectorBytes);
                    }, () -> {
                        log.warn("[FoodVectorService] 매칭 실패 - foodName='{}', gender='{}'", foodName, genderStr);
                        for (char c : foodName.toCharArray()) {
                            log.warn("char='{}', code={}", c, (int)c);
                        }
                    });
                }

                log.info("[FoodVectorService] 벡터 로딩 완료");
            }
        } catch (Exception e) {
            log.error("[FoodVectorService] 벡터 로딩 중 예외 발생: {}", e.getMessage(), e);
        }
    }

}
