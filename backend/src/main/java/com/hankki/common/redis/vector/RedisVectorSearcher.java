package com.hankki.common.redis.vector;

import com.hankki.common.redis.util.RedisVectorUtil;
import com.hankki.common.vector.VectorAggregator;
import com.hankki.domain.user.constant.Gender;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisVectorSearcher {

    private final RedisCommands<String, String> redisCommands; // FT.SEARCH 실행용

    /**
     * 주어진 음식 ID 리스트를 기반으로 벡터를 조회하고 평균 벡터 생성
     */
    public float[] computeAverageVector(List<Long> foodIds, Gender gender) {
        List<float[]> vectors = foodIds.stream()
                .map(id -> {
                    String key = String.format("food_%s:%d", gender.key(), id);
                    String encoded = redisCommands.get(key + "::vector");
                    if (encoded == null) throw new IllegalStateException("벡터가 존재하지 않음: " + key);
                    byte[] raw = Base64.getDecoder().decode(encoded);
                    return RedisVectorUtil.bytesToFloatArray(raw);
                })
                .collect(Collectors.toList());

        return VectorAggregator.average(vectors);
    }

    /**
     * Redis 벡터 인덱스에서 평균 벡터 기반으로 유사한 음식 ID를 검색
     * @param gender 성별 인덱스 구분용 키 (예: "male", "female")
     * @param queryVector 기준 벡터
     * @param topK 검색할 유사 음식 수
     * @return 유사한 foodId 리스트
     */
    public List<Long> knnSearch(Gender gender, float[] queryVector, int topK) {
        String index = String.format("idx:food_%s", gender.key());
        String base64Vec = Base64.getEncoder().encodeToString(RedisVectorUtil.floatArrayToBytes(queryVector));

        String query = String.format(
                "*=>[KNN %d @vector $vec_param] RETURN 1 food_id SORTBY __vector_score ASC LIMIT 0 %d",
                topK, topK
        );

        String command = String.format(
                "FT.SEARCH %s \"%s\" PARAMS 2 vec_param \"%s\" DIALECT 2",
                index, query, base64Vec
        );

        List<Object> result = redisCommands.dispatch(io.lettuce.core.protocol.CommandType.valueOf("FT.SEARCH"),
                new io.lettuce.core.output.ArrayOutput<>(io.lettuce.core.codec.StringCodec.UTF8),
                new io.lettuce.core.protocol.CommandArgs<>(io.lettuce.core.codec.StringCodec.UTF8)
                        .add(index)
                        .add(query)
                        .add("PARAMS").add(2).add("vec_param").add(base64Vec)
                        .add("DIALECT").add(2)
        );

        List<Long> foodIds = new ArrayList<>();
        for (int i = 1; i < result.size(); i += 2) {
            String key = (String) result.get(i);
            String[] parts = key.split(":");
            if (parts.length == 2) {
                foodIds.add(Long.parseLong(parts[1]));
            }
        }

        return foodIds;
    }
}
