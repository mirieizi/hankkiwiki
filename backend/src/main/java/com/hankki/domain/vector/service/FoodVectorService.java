package com.hankki.domain.vector.service;

import com.hankki.domain.vector.util.RedisVectorUtil;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.user.constant.Gender;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodVectorService {

    private static final int VECTOR_DIMENSION = 9;
    private static final String VECTOR_FILE_PATH = "/vectors/mini_food_embeddings.csv";

    private final FoodRepository foodRepository;
    private final RedisCommands<byte[], byte[]> redisCommands;

    // FT.INFO ProtocolKeyword 정의
    private static final ProtocolKeyword FT_INFO = new ProtocolKeyword() {
        private final byte[] raw = "FT.INFO".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT.INFO"; }
    };

    // FT._LIST ProtocolKeyword 정의 (Redis Stack 지원 확인용)
    private static final ProtocolKeyword FT_LIST = new ProtocolKeyword() {
        private final byte[] raw = "FT._LIST".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT._LIST"; }
    };

    public void loadVectors() {
        log.info("[FoodVectorService] 벡터 로딩 시작");

        // Redis Stack 지원 여부 먼저 확인
        if (!checkRedisStackSupport()) {
            log.error("[FoodVectorService] Redis Stack이 지원되지 않습니다. 벡터 로딩을 중단합니다.");
            return;
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        try (InputStream is = getClass().getResourceAsStream(VECTOR_FILE_PATH)) {
            if (is == null) {
                log.error("[FoodVectorService] 벡터 파일이 존재하지 않습니다: {}", VECTOR_FILE_PATH);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line = reader.readLine(); // skip header
                log.info("[FoodVectorService] 헤더 스킵: {}", line);

                while ((line = reader.readLine()) != null) {
                    try {
                        processVectorLine(line, successCount, failCount);
                    } catch (Exception e) {
                        log.warn("[FoodVectorService] 라인 처리 실패: '{}', 오류: {}", line, e.getMessage());
                        failCount.incrementAndGet();
                    }
                }

                log.info("[FoodVectorService] 벡터 로딩 완료 - 성공: {}, 실패: {}",
                        successCount.get(), failCount.get());

                // 안전한 인덱스 상태 확인
                checkIndexStatusSafely();

            }
        } catch (Exception e) {
            log.error("[FoodVectorService] 벡터 로딩 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    private boolean checkRedisStackSupport() {
        try {
            redisCommands.dispatch(
                    FT_LIST,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
            );
            log.info("[FoodVectorService] Redis Stack 모듈 지원 확인됨");
            return true;
        } catch (Exception e) {
            log.error("[FoodVectorService] Redis Stack 모듈 미지원: {}", e.getMessage());
            return false;
        }
    }

    private void processVectorLine(String line, AtomicInteger successCount, AtomicInteger failCount) {
        String[] tokens = line.split(",");

        if (tokens.length < VECTOR_DIMENSION + 2) {
            log.warn("[FoodVectorService] 토큰 수 부족: 예상={}, 실제={}", VECTOR_DIMENSION + 2, tokens.length);
            failCount.incrementAndGet();
            return;
        }

        String foodName = tokens[0].replaceAll("[\\s_]", "").trim();
        String genderStr = tokens[1].trim();

        Gender gender;
        try {
            gender = Gender.valueOf(genderStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("[FoodVectorService] 잘못된 gender 값: '{}'", genderStr);
            failCount.incrementAndGet();
            return;
        }

        double[] vector = new double[VECTOR_DIMENSION];
        try {
            for (int i = 0; i < VECTOR_DIMENSION; i++) {
                vector[i] = Double.parseDouble(tokens[i + 2]);
            }
        } catch (NumberFormatException ex) {
            log.warn("[FoodVectorService] 벡터 파싱 실패: '{}'", line);
            failCount.incrementAndGet();
            return;
        }

        foodRepository.findByFoodName(foodName).ifPresentOrElse(food -> {
            try {
                String redisKey = String.format("food_%s:%d", gender.key(), food.getId());
                byte[] vectorBytes = RedisVectorUtil.doubleToFloatBytes(vector);

                // 벡터 바이트 크기 검증
                if (vectorBytes.length != VECTOR_DIMENSION * 4) {
                    log.error("[FoodVectorService] 벡터 바이트 크기 오류: 예상={}, 실제={}",
                            VECTOR_DIMENSION * 4, vectorBytes.length);
                    failCount.incrementAndGet();
                    return;
                }

                redisCommands.hset(
                        redisKey.getBytes(StandardCharsets.UTF_8),
                        "vector".getBytes(StandardCharsets.UTF_8),
                        vectorBytes
                );

                // 저장 검증
                byte[] stored = redisCommands.hget(
                        redisKey.getBytes(StandardCharsets.UTF_8),
                        "vector".getBytes(StandardCharsets.UTF_8)
                );

                if (stored == null || stored.length != vectorBytes.length) {
                    log.error("[FoodVectorService] 벡터 저장 검증 실패: key={}", redisKey);
                    failCount.incrementAndGet();
                    return;
                }

                log.debug("[FoodVectorService] 벡터 저장 성공: key={}, 바이트 크기={}",
                        redisKey, vectorBytes.length);
                successCount.incrementAndGet();

            } catch (Exception e) {
                log.error("[FoodVectorService] 벡터 저장 중 오류: foodName={}, error={}",
                        foodName, e.getMessage());
                failCount.incrementAndGet();
            }

        }, () -> {
            log.warn("[FoodVectorService] 음식 매칭 실패: foodName='{}', gender='{}'", foodName, genderStr);
            failCount.incrementAndGet();
        });
    }

    private void checkIndexStatusSafely() {
        for (Gender gender : Gender.values()) {
            String indexName = "idx_" + gender.key();

            try {
                // ProtocolKeyword를 사용한 안전한 FT.INFO 호출
                Object result = redisCommands.dispatch(
                        FT_INFO,
                        new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                        new CommandArgs<>(ByteArrayCodec.INSTANCE)
                                .add(indexName.getBytes(StandardCharsets.UTF_8))
                );

                if (result instanceof java.util.List) {
                    java.util.List<?> infoList = (java.util.List<?>) result;
                    log.info("[FoodVectorService] 인덱스 상태 확인 - {}: 요소 수 {}", indexName, infoList.size());

                    // 문서 수 확인 (인덱스 정보에서 num_docs 찾기)
                    for (int i = 0; i < infoList.size() - 1; i++) {
                        if (infoList.get(i) instanceof byte[]) {
                            String key = new String((byte[]) infoList.get(i), StandardCharsets.UTF_8);
                            if ("num_docs".equals(key) && (i + 1) < infoList.size()) {
                                Object numDocs = infoList.get(i + 1);
                                log.info("[FoodVectorService] 인덱스 {} 문서 수: {}", indexName, numDocs);
                                break;
                            }
                        }
                    }
                } else {
                    log.info("[FoodVectorService] 인덱스 상태 확인 - {}: {}", indexName, result);
                }

            } catch (Exception e) {
                log.warn("[FoodVectorService] 인덱스 상태 확인 실패: index={}, error={}",
                        indexName, e.getMessage());

                // FT.INFO 실패 시 대안으로 샘플 키 확인
                checkSampleKeys(gender);
            }
        }
    }

    private void checkSampleKeys(Gender gender) {
        try {
            String sampleKeyPattern = String.format("food_%s:*", gender.key());
            log.info("[FoodVectorService] 대안 확인: {} 패턴의 키 존재 여부 확인", sampleKeyPattern);

            // 첫 번째 키 몇 개 확인
            for (int i = 1; i <= 5; i++) {
                String sampleKey = String.format("food_%s:%d", gender.key(), i);
                byte[] vectorData = redisCommands.hget(
                        sampleKey.getBytes(StandardCharsets.UTF_8),
                        "vector".getBytes(StandardCharsets.UTF_8)
                );

                if (vectorData != null) {
                    log.info("[FoodVectorService] {} 성별 벡터 확인: key={}, 크기={}",
                            gender, sampleKey, vectorData.length);
                    return; // 하나라도 찾으면 성공
                }
            }

            log.warn("[FoodVectorService] {} 성별 벡터 데이터를 찾을 수 없습니다", gender);

        } catch (Exception e) {
            log.error("[FoodVectorService] 샘플 키 확인 중 오류: {}", e.getMessage());
        }
    }
}
