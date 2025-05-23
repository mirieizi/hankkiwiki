package com.hankki.domain.vector.util;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.user.constant.Gender;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.ProtocolKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisVectorSearcher {

    private final RedisCommands<String, String> redisCommands; // FT.SEARCH 실행용

    /**
     * 주어진 음식 ID 리스트를 기반으로 벡터를 조회하고 평균 벡터 생성
     */
    public double[] computeAverageVector(List<Long> foodIds, Gender gender) {
        String genderKey = gender.key();
        log.debug("조회 대상 foodIds: {}", foodIds);
        log.debug("성별: {}", genderKey);

        List<Long> validFoodIds = foodIds.stream()
                .filter(id -> redisCommands.hexists(String.format("food_%s:%d", genderKey, id), "vector"))
                .toList();

        if (validFoodIds.isEmpty()) {
            log.error("[RedisVectorSearcher] 평균 벡터 계산 실패 - 유효한 벡터 없음. 요청 ID: {}", foodIds);
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
        }

        // ✅ 벡터 디코딩 및 평균 계산
        List<double[]> vectors = new ArrayList<>();
        for (Long id : validFoodIds) {
            String key = String.format("food_%s:%d", genderKey, id);
            String encoded = redisCommands.hget(key, "vector");

            try {
                byte[] raw = Base64.getDecoder().decode(encoded);
                vectors.add(RedisVectorUtil.bytesToDoubleArray(raw));
            } catch (Exception e) {
                log.error("[RedisVectorSearcher] 벡터 디코딩 실패 - key: {}, cause: {}", key, e.getMessage());
                throw new HankkiWikiException(ExceptionStatus.INVALID_INPUT_VALUE);
            }
        }

        return VectorAggregator.average(vectors);
    }

    private static final ProtocolKeyword FT_SEARCH = new ProtocolKeyword() {
        private final byte[] raw = "FT.SEARCH".getBytes(StandardCharsets.UTF_8);

        @Override
        public byte[] getBytes() {
            return raw;
        }

        @Override
        public String name() {
            return "FT.SEARCH";
        }
    };

    /**
     * Redis 벡터 인덱스에서 평균 벡터 기반으로 유사한 음식 ID를 검색
     */
    public List<Long> knnSearch(Gender gender, double[] queryVector, int topK) {
        String index = String.format("idx_%s", gender.key());
        String base64Vec = Base64.getEncoder().encodeToString(RedisVectorUtil.doubleArrayToBytes(queryVector));

        String query = String.format("*=>[KNN %d @vector $vec_param]", topK);

        if (log.isTraceEnabled()) {
            log.trace("[RedisVectorSearcher] FT.SEARCH 실행 - index={}, query={}, vec={}", index, query, base64Vec);
        }

        List<Object> result = redisCommands.dispatch(
                FT_SEARCH,
                new ArrayOutput<>(StringCodec.UTF8),
                new CommandArgs<>(StringCodec.UTF8)
                        .add(index)
                        .add(query)
                        .add("PARAMS").add(2).add("vec_param").add(base64Vec)
                        .add("SORTBY").add("__vector_score").add("DESC")
                        .add("RETURN").add(1).add("food_id")
                        .add("LIMIT").add(0).add(topK)
                        .add("DIALECT").add(2)
        );

        return extractIdsFromSearchResult(result);
    }

    /**
     * Redis 벡터 인덱스에서 평균 벡터 기반으로 가장 먼 음식 ID를 반환
     */
    public List<Long> furthestSearch(Gender gender, double[] queryVector, int topN) {
        String index = String.format("idx_%s", gender.key());
        String base64Vec = Base64.getEncoder().encodeToString(RedisVectorUtil.doubleArrayToBytes(queryVector));

        String query = String.format("*=>[KNN %d @vector $vec_param]", topN);

        List<Object> result = redisCommands.dispatch(
                FT_SEARCH,
                new ArrayOutput<>(StringCodec.UTF8),
                new CommandArgs<>(StringCodec.UTF8)
                        .add(index)
                        .add(query)
                        .add("PARAMS").add(2).add("vec_param").add(base64Vec)
                        .add("SORTBY").add("__vector_score").add("DESC")
                        .add("RETURN").add(1).add("food_id")
                        .add("LIMIT").add(0).add(topN)
                        .add("DIALECT").add(2)
        );

        return extractIdsFromSearchResult(result);
    }

    /**
     * Redis FT.SEARCH 결과에서 foodId만 추출
     */
    private List<Long> extractIdsFromSearchResult(List<Object> result) {
        List<Long> foodIds = new ArrayList<>();

        if (result == null || result.isEmpty()) {
            log.warn("[RedisVectorSearcher] 검색 결과가 null 또는 비어 있습니다.");
            return foodIds;
        }

        Object first = result.get(0);
        if (!(first instanceof Long total) || total == 0) {
            log.warn("[RedisVectorSearcher] 검색된 문서 수가 0입니다.");
            return foodIds;
        }

        for (int i = 1; i < result.size(); i += 2) {
            Object keyObj = result.get(i);

            if (keyObj instanceof String key) {
                log.debug("[RedisVectorSearcher] 검색된 키: {}", key);
                String[] parts = key.split(":");
                if (parts.length == 2) {
                    try {
                        foodIds.add(Long.parseLong(parts[1]));
                    } catch (NumberFormatException e) {
                        log.warn("[RedisVectorSearcher] 음식 ID 파싱 실패: key={}", key);
                    }
                } else {
                    log.warn("[RedisVectorSearcher] 예상치 못한 Redis 키 형식: {}", key);
                }
            } else {
                log.warn("[RedisVectorSearcher] 예상치 못한 키 타입: {}", keyObj == null ? "null" : keyObj.getClass().getName());
            }
        }

        return foodIds;
    }
}
