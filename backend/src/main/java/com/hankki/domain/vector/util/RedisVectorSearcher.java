package com.hankki.domain.vector.util;

import com.hankki.domain.user.constant.Gender;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    /**
     * KNN 검색 (가장 유사한 벡터들)
     */
    public List<Long> knnSearch(Gender gender, double[] queryVector, int limit) {
        if (!validateSearchInput(gender, queryVector, limit)) {
            return Collections.emptyList();
        }

        if (!checkRedisStackSupport()) {
            log.error("[RedisVectorSearcher] Redis Stack 미지원으로 검색 불가");
            return Collections.emptyList();
        }

        String indexName = "idx_" + gender.key();
        byte[] vectorBytes = RedisVectorUtil.doubleToFloatBytes(queryVector);

        try {
            log.debug("[RedisVectorSearcher] KNN 검색 시작: index={}, limit={}, vector_size={}",
                    indexName, limit, vectorBytes.length);

            // FT.SEARCH idx_male "*=>[KNN 30 @vector $BLOB]" PARAMS 2 BLOB vectorBytes RETURN 1 __key DIALECT 2
            Object result = redisCommands.dispatch(
                    FT_SEARCH,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
                            .add(indexName.getBytes(StandardCharsets.UTF_8))
                            .add(String.format("*=>[KNN %d @vector $BLOB]", limit).getBytes(StandardCharsets.UTF_8))
                            .add("PARAMS".getBytes(StandardCharsets.UTF_8)).add(2)
                            .add("BLOB".getBytes(StandardCharsets.UTF_8)).add(vectorBytes)
                            .add("RETURN".getBytes(StandardCharsets.UTF_8)).add(1).add("__key".getBytes(StandardCharsets.UTF_8))
                            .add("DIALECT".getBytes(StandardCharsets.UTF_8)).add(2)
            );

            List<Long> results = parseSearchResults(result, gender);
            log.info("[RedisVectorSearcher] KNN 검색 완료: index={}, 결과 수={}", indexName, results.size());
            return results;

        } catch (Exception e) {
            log.error("[RedisVectorSearcher] KNN 검색 실패: gender={}, error={}", gender, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 가장 먼 벡터 검색 (역순 정렬)
     */
    public List<Long> furthestSearch(Gender gender, double[] queryVector, int limit) {
        if (!validateSearchInput(gender, queryVector, limit)) {
            return Collections.emptyList();
        }

        try {
            // 더 많은 수를 가져온 후 거리순으로 정렬해서 뒤에서부터 가져오기
            List<Long> results = knnSearch(gender, queryVector, Math.max(limit * 3, 100));

            if (results.isEmpty()) {
                log.warn("[RedisVectorSearcher] KNN 검색 결과가 비어있어 furthest 검색 불가");
                return Collections.emptyList();
            }

            Collections.reverse(results); // 가장 먼 것부터
            List<Long> furthest = results.subList(0, Math.min(limit, results.size()));

            log.info("[RedisVectorSearcher] Furthest 검색 완료: gender={}, 결과 수={}", gender, furthest.size());
            return furthest;

        } catch (Exception e) {
            log.error("[RedisVectorSearcher] Furthest 검색 실패: gender={}, error={}", gender, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 여러 벡터의 평균 계산
     */
    public double[] computeAverageVector(List<Long> foodIds, Gender gender) {
        if (foodIds == null || foodIds.isEmpty()) {
            log.warn("[RedisVectorSearcher] 평균 벡터 계산할 음식 ID가 없습니다");
            return null;
        }

        log.debug("[RedisVectorSearcher] 평균 벡터 계산 시작: gender={}, foodIds 수={}", gender, foodIds.size());

        List<double[]> vectors = new ArrayList<>();
        int loadFailCount = 0;

        for (Long foodId : foodIds) {
            String redisKey = String.format("food_%s:%d", gender.key(), foodId);
            try {
                byte[] vectorBytes = redisCommands.hget(
                        redisKey.getBytes(StandardCharsets.UTF_8),
                        "vector".getBytes(StandardCharsets.UTF_8)
                );

                if (vectorBytes != null && vectorBytes.length == VECTOR_DIMENSION * 4) {
                    double[] vector = RedisVectorUtil.bytesToDoubleArray(vectorBytes);

                    if (RedisVectorUtil.isValidVector(vector, VECTOR_DIMENSION)) {
                        vectors.add(vector);
                        log.debug("[RedisVectorSearcher] 벡터 로드 성공: key={}", redisKey);
                    } else {
                        log.warn("[RedisVectorSearcher] 잘못된 벡터 데이터: key={}", redisKey);
                        loadFailCount++;
                    }
                } else {
                    log.warn("[RedisVectorSearcher] 벡터 로드 실패: key={}, bytes={}",
                            redisKey, vectorBytes != null ? vectorBytes.length : "null");
                    loadFailCount++;
                }
            } catch (Exception e) {
                log.error("[RedisVectorSearcher] 벡터 로드 중 오류: key={}, error={}", redisKey, e.getMessage());
                loadFailCount++;
            }
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

    private boolean checkRedisStackSupport() {
        try {
            redisCommands.dispatch(
                    FT_LIST,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
            );
            return true;
        } catch (Exception e) {
            log.error("[RedisVectorSearcher] Redis Stack 모듈 미지원: {}", e.getMessage());
            return false;
        }
    }

    private boolean validateSearchInput(Gender gender, double[] queryVector, int limit) {
        if (gender == null) {
            log.error("[RedisVectorSearcher] Gender가 null입니다");
            return false;
        }

        if (!RedisVectorUtil.isValidVector(queryVector, VECTOR_DIMENSION)) {
            log.error("[RedisVectorSearcher] 잘못된 쿼리 벡터: 차원={}, 예상={}",
                    queryVector != null ? queryVector.length : "null", VECTOR_DIMENSION);
            return false;
        }

        if (limit <= 0) {
            log.error("[RedisVectorSearcher] 잘못된 limit 값: {}", limit);
            return false;
        }

        return true;
    }

    /**
     * 검색 결과 파싱
     */
    private List<Long> parseSearchResults(Object result, Gender gender) {
        List<Long> foodIds = new ArrayList<>();

        try {
            if (result instanceof List) {
                List<?> resultList = (List<?>) result;
                log.debug("[RedisVectorSearcher] 검색 결과 크기: {}", resultList.size());

                // 첫 번째 요소는 총 결과 수
                if (resultList.size() > 1) {
                    for (int i = 1; i < resultList.size(); i += 2) { // key, attributes 쌍
                        if (resultList.get(i) instanceof byte[]) {
                            String key = new String((byte[]) resultList.get(i), StandardCharsets.UTF_8);
                            String prefix = "food_" + gender.key() + ":";

                            if (key.startsWith(prefix)) {
                                try {
                                    Long foodId = Long.parseLong(key.substring(prefix.length()));
                                    foodIds.add(foodId);
                                    log.debug("[RedisVectorSearcher] 파싱된 음식 ID: {}", foodId);
                                } catch (NumberFormatException e) {
                                    log.warn("[RedisVectorSearcher] 음식 ID 파싱 실패: key={}", key);
                                }
                            }
                        }
                    }
                } else {
                    log.warn("[RedisVectorSearcher] 검색 결과가 비어있습니다");
                }
            }
        } catch (Exception e) {
            log.error("[RedisVectorSearcher] 검색 결과 파싱 실패: {}", e.getMessage(), e);
        }

        log.info("[RedisVectorSearcher] 파싱된 음식 ID 수: {}", foodIds.size());
        return foodIds;
    }
}
