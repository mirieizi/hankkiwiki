package com.hankki.domain.vector.util;

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
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisVectorSearcher {

    private final RedisCommands<byte[], byte[]> redisCommands;

    private static final ProtocolKeyword FT_SEARCH = new ProtocolKeyword() {
        private final byte[] raw = "FT.SEARCH".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT.SEARCH"; }
    };

    private static final ProtocolKeyword FT_LIST = new ProtocolKeyword() {
        private final byte[] raw = "FT._LIST".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT._LIST"; }
    };

    private static final int VECTOR_DIMENSION = 9;
    private static volatile Boolean redisStackSupported = null;

    /**
     * KNN 검색 (가장 유사한 벡터들)
     */
    public List<Long> knnSearch(Gender gender, double[] queryVector, int limit) {
        if (!validateSearchInput(gender, queryVector, limit)) {
            log.warn("[RedisVectorSearcher] KNN 입력값이 유효하지 않음 - gender={}, vectorValid={}, limit={}",
                    gender, RedisVectorUtil.isValidVector(queryVector, VECTOR_DIMENSION), limit);
            return Collections.emptyList();        }
        if (!checkRedisStackSupport()) return Collections.emptyList();

        log.info("[RedisVectorSearcher] KNN 검색 시작 - gender={}, limit={}, queryVector={}", gender, limit, Arrays.toString(queryVector));

        String indexName = "idx_" + gender.key();
        byte[] vectorBytes = RedisVectorUtil.doubleToFloatBytes(queryVector);

        try {
            Object result = redisCommands.dispatch(
                    FT_SEARCH,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
                            .add(indexName.getBytes(StandardCharsets.UTF_8))
                            .add(String.format("*=>[KNN %d @vector $BLOB]", limit).getBytes(StandardCharsets.UTF_8))
                            .add("PARAMS".getBytes(StandardCharsets.UTF_8)).add(2)
                            .add("BLOB".getBytes(StandardCharsets.UTF_8)).add(vectorBytes)
                            .add("RETURN".getBytes(StandardCharsets.UTF_8)).add(1).add("__key".getBytes(StandardCharsets.UTF_8))
                            .add("SORTBY".getBytes(StandardCharsets.UTF_8)).add("__vector_score".getBytes(StandardCharsets.UTF_8)).add("ASC".getBytes(StandardCharsets.UTF_8))
                            .add("DIALECT".getBytes(StandardCharsets.UTF_8)).add(2)
            );
            return parseSearchResults(result, gender, limit);

        } catch (Exception e) {
            log.error("[RedisVectorSearcher] KNN 검색 실패: gender={}, error={}", gender, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 가장 먼 벡터 검색 (Redis에서 내림차순 정렬)
     */
    public List<Long> furthestSearch(Gender gender, double[] queryVector, int limit) {
        if (!validateSearchInput(gender, queryVector, limit)) return Collections.emptyList();
        if (!checkRedisStackSupport()) return Collections.emptyList();

        String indexName = "idx_" + gender.key();
        byte[] vectorBytes = RedisVectorUtil.doubleToFloatBytes(queryVector);

        try {
            Object result = redisCommands.dispatch(
                    FT_SEARCH,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
                            .add(indexName.getBytes(StandardCharsets.UTF_8))
                            .add(String.format("*=>[KNN %d @vector $BLOB]", limit).getBytes(StandardCharsets.UTF_8))
                            .add("PARAMS".getBytes(StandardCharsets.UTF_8)).add(2)
                            .add("BLOB".getBytes(StandardCharsets.UTF_8)).add(vectorBytes)
                            .add("RETURN".getBytes(StandardCharsets.UTF_8)).add(1).add("__key".getBytes(StandardCharsets.UTF_8))
                            .add("SORTBY".getBytes(StandardCharsets.UTF_8)).add("__vector_score".getBytes(StandardCharsets.UTF_8)).add("DESC".getBytes(StandardCharsets.UTF_8))
                            .add("DIALECT".getBytes(StandardCharsets.UTF_8)).add(2)
            );
            return parseSearchResults(result, gender, limit);

        } catch (Exception e) {
            log.error("[RedisVectorSearcher] Furthest 검색 실패: gender={}, error={}", gender, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 여러 벡터의 평균 계산 (파이프라인 사용)
     */
    public double[] computeAverageVector(List<Long> foodIds, Gender gender) {
        if (foodIds == null || foodIds.isEmpty()) {
            log.warn("[RedisVectorSearcher] 평균 벡터 계산할 음식 ID가 없습니다");
            return null;
        }
        log.info("[RedisVectorSearcher] 평균 벡터 계산 시작 - foodIds={}, gender={}", foodIds, gender);

        List<double[]> vectors = new ArrayList<>(foodIds.size());
        int loadFailCount = 0;

        // 파이프라인(비동기)으로 벡터 일괄 조회
        try {
            RedisAsyncCommands<byte[], byte[]> async = redisCommands.getStatefulConnection().async();
            List<RedisFuture<byte[]>> futures = new ArrayList<>(foodIds.size());
            for (Long foodId : foodIds) {
                String redisKey = String.format("food_%s:%d", gender.key(), foodId);
                futures.add(async.hget(redisKey.getBytes(StandardCharsets.UTF_8), "vector".getBytes(StandardCharsets.UTF_8)));
            }
            for (int i = 0; i < foodIds.size(); i++) {
                byte[] vectorBytes = futures.get(i).get(5, TimeUnit.SECONDS);
                if (vectorBytes != null && vectorBytes.length == VECTOR_DIMENSION * 4) {
                    double[] vector = RedisVectorUtil.bytesToDoubleArray(vectorBytes);
                    if (RedisVectorUtil.isValidVector(vector, VECTOR_DIMENSION)) {
                        vectors.add(vector);
                    } else {
                        loadFailCount++;
                    }
                } else {
                    loadFailCount++;
                }
            }
        } catch (Exception e) {
            log.error("[RedisVectorSearcher] 평균 벡터 파이프라인 조회 중 오류: {}", e.getMessage());
            return null;
        }

        if (vectors.isEmpty()) {
            log.error("[RedisVectorSearcher] 로드된 벡터가 없습니다. 실패 수: {}", loadFailCount);
            return null;
        }

        // 평균 계산
        double[] avgVector = new double[VECTOR_DIMENSION];
        for (double[] vector : vectors) {
            for (int i = 0; i < VECTOR_DIMENSION; i++) {
                avgVector[i] += vector[i];
            }
        }
        for (int i = 0; i < VECTOR_DIMENSION; i++) {
            avgVector[i] /= vectors.size();
        }
        log.info("[RedisVectorSearcher] 평균 벡터 계산 완료: 성공={}, 실패={}", vectors.size(), loadFailCount);
        return avgVector;
    }

    // Redis Stack 지원 체크(캐싱)
    private boolean checkRedisStackSupport() {
        if (redisStackSupported != null) return redisStackSupported;
        synchronized (RedisVectorSearcher.class) {
            if (redisStackSupported != null) return redisStackSupported;
            try {
                redisCommands.dispatch(
                        FT_LIST,
                        new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                        new CommandArgs<>(ByteArrayCodec.INSTANCE)
                );
                redisStackSupported = true;
            } catch (Exception e) {
                log.error("[RedisVectorSearcher] Redis Stack 모듈 미지원: {}", e.getMessage());
                redisStackSupported = false;
            }
            return redisStackSupported;
        }
    }

    private boolean validateSearchInput(Gender gender, double[] queryVector, int limit) {
        if (gender == null) return false;
        if (!RedisVectorUtil.isValidVector(queryVector, VECTOR_DIMENSION)) return false;
        if (limit <= 0) return false;
        return true;
    }

    /**
     * 검색 결과 파싱 (최대 limit개)
     */
    private List<Long> parseSearchResults(Object result, Gender gender, int limit) {
        List<Long> foodIds = new ArrayList<>(limit);
        try {
            if (result instanceof List) {
                List<?> resultList = (List<?>) result;
                // 첫 번째 요소는 총 결과 수
                for (int i = 1; i < resultList.size() && foodIds.size() < limit; i += 2) { // key, attributes 쌍
                    if (resultList.get(i) instanceof byte[]) {
                        String key = new String((byte[]) resultList.get(i), StandardCharsets.UTF_8);
                        String prefix = "food_" + gender.key() + ":";
                        if (key.startsWith(prefix)) {
                            try {
                                Long foodId = Long.parseLong(key.substring(prefix.length()));
                                foodIds.add(foodId);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
            log.debug("[RedisVectorSearcher] KNN 결과: {}", foodIds);
        } catch (Exception e) {
            log.error("[RedisVectorSearcher] 검색 결과 파싱 실패: {}", e.getMessage(), e);
        }
        return foodIds;
    }
}
