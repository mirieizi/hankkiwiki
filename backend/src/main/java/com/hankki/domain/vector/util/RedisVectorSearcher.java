package com.hankki.domain.vector.util;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.user.constant.Gender;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.protocol.CommandType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        List<double[]> vectors = new ArrayList<>();

        for (Long id : foodIds) {
            String key = String.format("food_%s:%d", gender.key(), id);
            String encoded = redisCommands.hget(key, "vector");

            if (encoded == null) {
                log.error("[RedisVectorSearcher] 벡터를 찾지 못했습니다. key: {}, gender: {}, id: {}", key, gender, id);
                throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
            }

            try {
                byte[] raw = Base64.getDecoder().decode(encoded);
                vectors.add(RedisVectorUtil.bytesToDoubleArray(raw));
            } catch (Exception e) {
                log.error("[RedisVectorSearcher] 벡터 디코딩 실패: key={}, cause={}", key, e.getMessage());
                throw new HankkiWikiException(ExceptionStatus.INVALID_INPUT_VALUE);
            }
        }

        return VectorAggregator.average(vectors);
    }

    /**
     * Redis 벡터 인덱스에서 평균 벡터 기반으로 유사한 음식 ID를 검색
     */
    public List<Long> knnSearch(Gender gender, double[] queryVector, int topK) {
        String index = String.format("idx:food_%s", gender.key());
        String base64Vec = Base64.getEncoder().encodeToString(RedisVectorUtil.doubleArrayToBytes(queryVector));

        String query = String.format(
                "*=>[KNN %d @vector $vec_param] RETURN 1 food_id SORTBY __vector_score ASC LIMIT 0 %d",
                topK, topK
        );

        if (log.isTraceEnabled()) {
            log.trace("[RedisVectorSearcher] FT.SEARCH command: " +
                    "FT.SEARCH {} \"{}\" PARAMS 2 vec_param \"{}\" DIALECT 2", index, query, base64Vec);
        }

        List<Object> result = redisCommands.dispatch(CommandType.valueOf("FT.SEARCH"),
                new io.lettuce.core.output.ArrayOutput<>(io.lettuce.core.codec.StringCodec.UTF8),
                new io.lettuce.core.protocol.CommandArgs<>(io.lettuce.core.codec.StringCodec.UTF8)
                        .add(index)
                        .add(query)
                        .add("PARAMS").add(2).add("vec_param").add(base64Vec)
                        .add("DIALECT").add(2)
        );

        return extractIdsFromSearchResult(result);
    }

    /**
     * Redis 벡터 인덱스에서 평균 벡터 기반으로 가장 먼 음식 ID를 반환
     */
    public List<Long> furthestSearch(Gender gender, double[] queryVector, int topN) {
        String index = String.format("idx:food_%s", gender.key());
        String base64Vec = Base64.getEncoder().encodeToString(RedisVectorUtil.doubleArrayToBytes(queryVector));

        String query = String.format(
                "*=>[KNN %d @vector $vec_param] RETURN 1 food_id SORTBY __vector_score DESC LIMIT 0 %d",
                topN, topN
        );

        List<Object> result = redisCommands.dispatch(CommandType.valueOf("FT.SEARCH"),
                new io.lettuce.core.output.ArrayOutput<>(io.lettuce.core.codec.StringCodec.UTF8),
                new io.lettuce.core.protocol.CommandArgs<>(io.lettuce.core.codec.StringCodec.UTF8)
                        .add(index)
                        .add(query)
                        .add("PARAMS").add(2).add("vec_param").add(base64Vec)
                        .add("DIALECT").add(2)
        );

        List<Long> ids = extractIdsFromSearchResult(result);

        if (ids.isEmpty()) {
            throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
        }

        return ids;
    }

    /**
     * Redis FT.SEARCH 결과에서 foodId만 추출
     */
    private List<Long> extractIdsFromSearchResult(List<Object> result) {
        List<Long> foodIds = new ArrayList<>();
        for (int i = 1; i < result.size(); i += 2) {
            String key = (String) result.get(i);
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
        }
        return foodIds;
    }
}
