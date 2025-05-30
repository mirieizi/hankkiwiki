package com.hankki.domain.vector.service;

import com.hankki.domain.vector.util.RedisVectorUtil;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.user.constant.Gender;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    /**
     * 벡터 일괄 적재 (파이프라인) 및 일부 샘플 검증
     */
    public void loadVectors() {
        log.info("[FoodVectorService] 벡터 로딩 시작");

        // Redis Stack 지원 여부 먼저 확인
        if (!checkRedisStackSupport()) {
            log.error("[FoodVectorService] Redis Stack이 지원되지 않습니다. 벡터 로딩을 중단합니다.");
            return;
        }

        Map<String, byte[]> redisKeyToVector = new LinkedHashMap<>();
        Map<String, String> keyToFoodName = new HashMap<>();

        try (InputStream is = getClass().getResourceAsStream(VECTOR_FILE_PATH)) {
            if (is == null) {
                log.error("[FoodVectorService] 벡터 파일이 존재하지 않습니다: {}", VECTOR_FILE_PATH);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line = reader.readLine(); // skip header
                log.info("[FoodVectorService] 헤더 스킵: {}", line);

                int lineNum = 1;
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    Optional<VectorEntry> entryOpt = parseVectorLine(line);
                    if (entryOpt.isEmpty()) {
                        log.warn("[FoodVectorService] {}번째 라인 파싱 실패, 스킵", lineNum);
                        continue;
                    }
                    VectorEntry entry = entryOpt.get();
                    foodRepository.findByFoodName(entry.foodName).ifPresentOrElse(food -> {
                        String redisKey = String.format("food_%s:%d", entry.gender.key(), food.getId());
                        redisKeyToVector.put(redisKey, entry.vectorBytes);
                        keyToFoodName.put(redisKey, entry.foodName);
                    }, () -> log.warn("[FoodVectorService] 음식 매칭 실패: foodName='{}', gender='{}'", entry.foodName, entry.gender));
                }
            }
        } catch (Exception e) {
            log.error("[FoodVectorService] 벡터 파일 파싱 중 예외: {}", e.getMessage(), e);
            return;
        }

        // Redis 파이프라인(비동기) 일괄 적재
        if (redisKeyToVector.isEmpty()) {
            log.warn("[FoodVectorService] 적재할 벡터 데이터가 없습니다.");
            return;
        }

        try {
            RedisAsyncCommands<byte[], byte[]> async = redisCommands.getStatefulConnection().async();
            List<RedisFuture<?>> futures = new ArrayList<>();
            redisKeyToVector.forEach((redisKey, vectorBytes) -> {
                futures.add(async.hset(redisKey.getBytes(StandardCharsets.UTF_8), "vector".getBytes(StandardCharsets.UTF_8), vectorBytes));
            });

            // 모든 적재 완료 대기 (최대 10초)
            for (RedisFuture<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
            log.info("[FoodVectorService] 벡터 {}개 일괄 적재 완료", redisKeyToVector.size());
        } catch (Exception e) {
            log.error("[FoodVectorService] Redis 파이프라인 적재 중 예외: {}", e.getMessage(), e);
            return;
        }

        // 샘플 3개만 저장 검증
        int checked = 0;
        for (String redisKey : redisKeyToVector.keySet()) {
            if (checked >= 3) break;
            byte[] stored = redisCommands.hget(redisKey.getBytes(StandardCharsets.UTF_8), "vector".getBytes(StandardCharsets.UTF_8));
            if (stored == null || stored.length != VECTOR_DIMENSION * 4) {
                log.error("[FoodVectorService] 샘플 벡터 저장 검증 실패: key={}, foodName={}", redisKey, keyToFoodName.get(redisKey));
            } else {
                log.info("[FoodVectorService] 샘플 벡터 저장 검증 성공: key={}, foodName={}", redisKey, keyToFoodName.get(redisKey));
            }
            checked++;
        }

        // 인덱스 상태 확인
        checkIndexStatusSafely();
    }

    /**
     * 벡터 라인을 파싱하여 VectorEntry로 반환
     */
    private Optional<VectorEntry> parseVectorLine(String line) {
        String[] tokens = line.split(",");
        if (tokens.length < VECTOR_DIMENSION + 2) return Optional.empty();

        String foodName = tokens[0].replaceAll("[\\s_]", "").trim();
        String genderStr = tokens[1].trim();
        Gender gender;
        try {
            gender = Gender.valueOf(genderStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        double[] vector = new double[VECTOR_DIMENSION];
        try {
            for (int i = 0; i < VECTOR_DIMENSION; i++) {
                vector[i] = Double.parseDouble(tokens[i + 2]);
            }
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }

        byte[] vectorBytes = RedisVectorUtil.doubleToFloatBytes(vector);
        if (vectorBytes.length != VECTOR_DIMENSION * 4) return Optional.empty();

        return Optional.of(new VectorEntry(foodName, gender, vectorBytes));
    }

    /**
     * Redis Stack 지원 여부 확인
     */
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

    /**
     * 인덱스 상태 확인 (효율적으로)
     */
    private void checkIndexStatusSafely() {
        // 여러 gender를 하나의 인덱스에 PREFIX로 관리한다면 아래처럼 하나만 확인
        String indexName = "idx_food_vector";
        try {
            Object result = redisCommands.dispatch(
                    FT_INFO,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
                            .add(indexName.getBytes(StandardCharsets.UTF_8))
            );

            if (result instanceof List) {
                List<?> infoList = (List<?>) result;
                log.info("[FoodVectorService] 인덱스 상태 확인 - {}: 요소 수 {}", indexName, infoList.size());
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
            log.warn("[FoodVectorService] 인덱스 상태 확인 실패: index={}, error={}", indexName, e.getMessage());
        }
    }

    /**
     * 벡터 데이터 파싱용 내부 클래스
     */
    private static class VectorEntry {
        final String foodName;
        final Gender gender;
        final byte[] vectorBytes;
        VectorEntry(String foodName, Gender gender, byte[] vectorBytes) {
            this.foodName = foodName;
            this.gender = gender;
            this.vectorBytes = vectorBytes;
        }
    }
}
